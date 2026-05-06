import React, { useState } from 'react';
import { Alert, Text } from 'react-native';
import Screen from '../components/Screen';
import { Button } from '../components/FormControls';
import { unlockCapsule } from '../api/capsules';

export default function CapsuleDetailScreen({ route }) {
  const { capsule, currentLocation } = route.params;
  const [unlocked, setUnlocked] = useState(null);

  const handleUnlock = async () => {
    try {
      const { data } = await unlockCapsule(capsule.id, {
        latitude: currentLocation.latitude,
        longitude: currentLocation.longitude
      });
      setUnlocked(data);
    } catch (e) {
      Alert.alert('Unlock failed', e.response?.data?.message || e.message);
    }
  };

  return (
    <Screen>
      <Text style={{ fontSize: 24, fontWeight: '700' }}>Capsule nearby</Text>
      <Text>Distance: {Math.round(capsule.distanceMeters)} meters</Text>
      <Button title="Try unlock" onPress={handleUnlock} />
      {unlocked && (
        <>
          <Text style={{ fontSize: 18, fontWeight: '700' }}>Unlocked</Text>
          {!!unlocked.textContent && <Text>{unlocked.textContent}</Text>}
          {(unlocked.media || []).map(item => (
            <Text key={item.mediaId}>{item.mediaType}: {item.signedUrl}</Text>
          ))}
        </>
      )}
    </Screen>
  );
}
