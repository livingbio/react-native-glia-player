import React, { useMemo, useRef } from 'react';
import { View, ScrollView, StyleSheet, Button } from 'react-native';
import { GliaPlayerView, Commands } from 'react-native-glia-player';

const getRandomColor = () => {
  const r = Math.floor(Math.random() * 256);
  const g = Math.floor(Math.random() * 256);
  const b = Math.floor(Math.random() * 256);
  return `rgb(${r}, ${g}, ${b})`;
};

export default function App() {
  const playerRef = useRef(null);

  const handlePause = () => {
    if (playerRef.current) {
      Commands.pause(playerRef.current);
    }
  };

  const handleResume = () => {
    if (playerRef.current) {
      Commands.resume(playerRef.current);
    }
  };
  const dummyBoxes = useMemo(() => {
    return Array.from({ length: 100 }).map((_, index) => (
      <View key={index} style={styles.boxContainer}>
        <View
          style={[styles.randomBox, { backgroundColor: getRandomColor() }]}
        />
      </View>
    ));
  }, []);

  return (
    <View style={styles.container}>
      <View style={styles.buttonContainer}>
        <Button title="Pause" onPress={handlePause} />
        <Button title="Resume" onPress={handleResume} />
      </View>
      <ScrollView style={styles.scrollView}>{dummyBoxes}</ScrollView>
      <View style={styles.playerContainer}>
        <GliaPlayerView
          ref={playerRef}
          slotKey="gliacloud_app_test"
          style={styles.gliaplayer}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    padding: 100,
  },
  scrollView: {
    flex: 1,
  },
  boxContainer: {
    marginBottom: 16,
  },
  randomBox: {
    width: '100%',
    height: 320,
  },
  playerContainer: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    width: 320,
    height: 240,
    elevation: 5,
    zIndex: 5,
  },
  gliaplayer: {
    flex: 1,
    width: '100%',
    height: '100%',
  },
});
