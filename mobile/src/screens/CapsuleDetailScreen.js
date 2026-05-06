import React, { useState } from 'react';
import { Alert, Linking, StyleSheet, Text, View } from 'react-native';
import Screen from '../components/Screen';
import { Button } from '../components/FormControls';
import { unlockCapsule } from '../api/capsules';
import { palette } from '../theme/palette';

export default function CapsuleDetailScreen({ route }) {
  const { capsule, currentLocation } = route.params;
  const [unlocked, setUnlocked] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleUnlock = async () => {
    // Guard: currentLocation must exist before sending coordinates.
    if (!currentLocation) {
      Alert.alert('Location unavailable', 'Cannot unlock without a GPS fix.');
      return;
    }
    setLoading(true);
    try {
      const { data } = await unlockCapsule(capsule.id, {
        latitude: currentLocation.latitude,
        longitude: currentLocation.longitude,
      });
      setUnlocked(data);
    } catch (e) {
      Alert.alert('Unlock failed', e.response?.data?.message || e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Screen>
      <Text style={styles.title}>Capsule nearby</Text>
      <Text style={styles.dist}>
        📦 {Math.round(capsule.distanceMeters)} metres away
      </Text>

      {unlocked ? (
        <View style={styles.unlockedBox}>
          <Text style={styles.unlockedHeader}>🔓 Unlocked!</Text>
          {!!unlocked.textContent && (
            <Text style={styles.unlockedText}>{unlocked.textContent}</Text>
          )}
          {(unlocked.media || []).map(item => (
            <View key={item.mediaId} style={styles.mediaRow}>
              <Text style={styles.mediaType}>{item.mediaType}</Text>
              {/* Allow the user to open the signed URL in the browser or a
                  media viewer. In a production app, render Image / Video. */}
              <Text
                style={styles.mediaLink}
                onPress={() => Linking.openURL(item.signedUrl)}
                numberOfLines={1}
                ellipsizeMode="middle"
              >
                View media ›
              </Text>
            </View>
          ))}
        </View>
      ) : (
        <Button
          title="Try unlock"
          onPress={handleUnlock}
          loading={loading}
        />
      )}
    </Screen>
  );
}

const styles = StyleSheet.create({
  title: { fontSize: 24, fontWeight: '700', color: palette.text },
  dist: { fontSize: 15, color: palette.primary, fontWeight: '600' },
  unlockedBox: {
    backgroundColor: palette.surfaceAlt,
    borderRadius: 12,
    padding: 16,
    gap: 12,
    borderWidth: 1,
    borderColor: palette.border,
  },
  unlockedHeader: { fontSize: 20, fontWeight: '700', color: palette.success },
  unlockedText: { fontSize: 16, color: palette.text, lineHeight: 24 },
  mediaRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  mediaType: { fontSize: 13, color: palette.muted, textTransform: 'capitalize' },
  mediaLink: { fontSize: 13, color: palette.primary, fontWeight: '600', flex: 1, marginLeft: 8 },
});
