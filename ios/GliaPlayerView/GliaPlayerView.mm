#import "GliaPlayerView.h"

#import <react/renderer/components/GliaPlayerViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/GliaPlayerViewSpec/EventEmitters.h>
#import <react/renderer/components/GliaPlayerViewSpec/Props.h>
#import <react/renderer/components/GliaPlayerViewSpec/RCTComponentViewHelpers.h>

#import <WebKit/WebKit.h>
#import <GoogleMobileAds/GoogleMobileAds.h>
#import <SafariServices/SafariServices.h>

using namespace facebook::react;

@interface GliaPlayerView () <RCTGliaPlayerViewViewProtocol, WKNavigationDelegate, WKUIDelegate>
@end

@implementation GliaPlayerView {
  NSString * _slotKey;
  WKWebView * _webView;
}

- (instancetype)initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    static const auto defaultProps = std::make_shared<const GliaPlayerViewProps>();
    _props = defaultProps;
    
    // Initialize a WKWebViewConfiguration object.
    WKWebViewConfiguration *config = [[WKWebViewConfiguration alloc] init];
    // Let HTML videos with a "playsinline" attribute play inline.
    config.allowsInlineMediaPlayback = YES;
    // Let HTML videos with an "autoplay" attribute play automatically.
    config.mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeNone;
    
    // Initialize the WKWebView with WKWebViewConfiguration object.
    _webView = [[WKWebView alloc] initWithFrame:self.bounds configuration:config];
    _webView.UIDelegate = self;
    _webView.navigationDelegate = self;
    _webView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

    [self addSubview:_webView];
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        [GADMobileAds.sharedInstance startWithCompletionHandler:nil];
    });
    // Register the web view.
    [GADMobileAds.sharedInstance registerWebView:_webView];
  }
  return self;
}

- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
    const auto &oldViewProps = *std::static_pointer_cast<GliaPlayerViewProps const>(_props);
    const auto &newViewProps = *std::static_pointer_cast<GliaPlayerViewProps const>(props);

    if (oldViewProps.slotKey != newViewProps.slotKey) {
      _slotKey = [NSString stringWithUTF8String:newViewProps.slotKey.c_str()];
        if (_slotKey.length > 0) {
          NSString *urlString = [NSString stringWithFormat:@"https://player.gliacloud.com/in-app-browser/%@", _slotKey];
          NSURL *url = [NSURL URLWithString:urlString];
          NSURLRequest *request = [NSURLRequest requestWithURL:url];
          [_webView loadRequest:request];
        }
    }

    [super updateProps:props oldProps:oldProps];
}

#pragma mark - React Native Commands

// Route the JS commands to the native Objective-C methods
- (void)handleCommand:(const NSString *)commandName args:(const NSArray *)args {
  RCTGliaPlayerViewHandleCommand(self, commandName, args);
}

// Pause Implementation
- (void)pause {
    if (_webView) {
        // If you are on iOS 15+, this is a more "native" way to halt the WebView:
        if (@available(iOS 15.0, *)) {
            [_webView setAllMediaPlaybackSuspended:YES completionHandler:nil];
        } else {
          // We use JS as a universal way to pause the HTML5 player
          [_webView evaluateJavaScript:@"document.querySelectorAll('video').forEach(v => v.pause());" 
                    completionHandler:nil];
        }
    }
}

// Resume Implementation
- (void)resume {
    if (_webView) {
        if (@available(iOS 15.0, *)) {
            [_webView setAllMediaPlaybackSuspended:NO completionHandler:nil];
        } else {
          [_webView evaluateJavaScript:@"document.querySelectorAll('video').forEach(v => v.play());" 
                   completionHandler:nil];
        }
    }
}

#pragma mark - Helper for Safari Presentation

// Helper to find the current ViewController to present Safari
- (UIViewController *)parentViewController {
    UIResponder *responder = self;
    while ([responder nextResponder] != nil) {
        responder = [responder nextResponder];
        if ([responder isKindOfClass:[UIViewController class]]) {
            return (UIViewController *)responder;
        }
    }
    return nil;
}

// Implement a helper method to handle click behavior.
- (BOOL)didHandleClickBehaviorForCurrentURL:(NSURL *)currentURL
                    navigationAction:(WKNavigationAction *)navigationAction {
  NSURL *targetURL = navigationAction.request.URL;

  // Handle custom URL schemes such as itms-apps:// by attempting to
  // launch the corresponding application.
  if (navigationAction.navigationType == WKNavigationTypeLinkActivated) {
    NSString *scheme = targetURL.scheme;
    if (![scheme isEqualToString:@"http"] && ![scheme isEqualToString:@"https"]) {
      [UIApplication.sharedApplication openURL:targetURL options:@{} completionHandler:nil];
      return YES;
    }
  }

  NSString *currentDomain = currentURL.host;
  NSString *targetDomain = targetURL.host;

  if (!currentDomain || !targetDomain) {
    return NO;
  }

  // Check if the navigationType is a link with an href attribute or
  // if the target of the navigation is a new window.
  if ((navigationAction.navigationType == WKNavigationTypeLinkActivated
      || !navigationAction.targetFrame)
      // If the current domain does not equal the target domain,
      // the assumption is the user is navigating away from the site.
      && ![currentDomain isEqualToString: targetDomain]) {
     // 4. Open the URL in a SFSafariViewController.
    SFSafariViewController *safariViewController =
        [[SFSafariViewController alloc] initWithURL:targetURL];
    [[self parentViewController] presentViewController:safariViewController animated:YES completion:nil];
    return YES;
  }

  return NO;
}


// Implement the WKUIDelegate method.
- (WKWebView *)webView:(WKWebView *)webView
  createWebViewWithConfiguration:(WKWebViewConfiguration *)configuration
             forNavigationAction:(WKNavigationAction *)navigationAction
                  windowFeatures:(WKWindowFeatures *)windowFeatures {
  // 3. Determine whether to optimize the behavior of the click URL.
  if ([self didHandleClickBehaviorForCurrentURL: webView.URL
      navigationAction: navigationAction]) {
    NSLog(@"URL opened in SFSafariViewController.");
  }

  return nil;
}

// Implement the WKNavigationDelegate method.
- (void)webView:(WKWebView *)webView
    decidePolicyForNavigationAction:(WKNavigationAction *)navigationAction
                    decisionHandler:
                        (void (^)(WKNavigationActionPolicy))decisionHandler {
  // 3. Determine whether to optimize the behavior of the click URL.
  if ([self didHandleClickBehaviorForCurrentURL: webView.URL
      navigationAction: navigationAction]) {
    decisionHandler(WKNavigationActionPolicyCancel);
    return;
  }

  decisionHandler(WKNavigationActionPolicyAllow);
}

- (BOOL)urlIsValid:(std::string)propString
{
  if (propString.length() > 0 && !_slotKey) {
    GliaPlayerViewEventEmitter::OnPageLoaded result = GliaPlayerViewEventEmitter::OnPageLoaded{GliaPlayerViewEventEmitter::OnPageLoadedResult::Error};

    self.eventEmitter.onPageLoaded(result);
    return NO;
  }
  return YES;
}

#pragma mark - WKNavigationDelegate

-(void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation
{
  GliaPlayerViewEventEmitter::OnPageLoaded result = GliaPlayerViewEventEmitter::OnPageLoaded{GliaPlayerViewEventEmitter::OnPageLoadedResult::Success};
  self.eventEmitter.onPageLoaded(result);
}

-(void)layoutSubviews
{
  [super layoutSubviews];
  _webView.frame = self.bounds;

}

// Event emitter convenience method
- (const GliaPlayerViewEventEmitter &)eventEmitter
{
  return static_cast<const GliaPlayerViewEventEmitter &>(*_eventEmitter);
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
  return concreteComponentDescriptorProvider<GliaPlayerViewComponentDescriptor>();
}

Class<RCTComponentViewProtocol> GliaPlayerViewCls(void) {
    return GliaPlayerView.class;
}

@end
