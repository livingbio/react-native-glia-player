require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))
min_ios_version_supported = "13.4"

Pod::Spec.new do |s|
  s.name         = "GliaPlayer"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => min_ios_version_supported }
  s.source       = { :git => "https://github.com/livingbio/react-native-glia-player.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,swift,cpp}"
  s.private_header_files = "ios/**/*.h"
  s.dependency "Google-Mobile-Ads-SDK"
  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'SWIFT_COMPILATION_MODE' => 'wholemodule',
  }
  s.swift_version = "5.0"

  install_modules_dependencies(s)
end
