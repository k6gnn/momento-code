import React, { useEffect, useState } from 'react';
import { Alert, ScrollView, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import Screen from '../components/Screen';
import useLocation from '../hooks/useLocation';
import { getNearbyCapsules } from '../api/capsules';
import { palette } from '../theme/palette';

export default function MapScreen({ navigation }) {
  const { location, error } = useLocation();
  const [capsules, setCapsules] = useState([]);
  const [refreshing, setRefreshing] = useState(false);

  const load = () => {
    if (!location) return;
    setRefreshing(true);
    getNearbyCapsules(location.latitude, location.longitude)
      .then(({ data }) => setCapsules(data))
      .catch(e => Alert.alert('Error', e.message))
      .finally(() => setRefreshing(false));
  };

  useEffect(() => { load(); }, [location]);

  if (error) return <Screen><Text style={styles.msg}>{error}</Text></Screen>;
  if (!location) return <Screen><Text style={styles.msg}>Getting your location…</Text></Screen>;

  return (
    <Screen scroll={false}>
      <View style={styles.locationBadge}>
        <Text style={styles.locationText}>
          📍 {location.latitude.toFixed(5)}, {location.longitude.toFixed(5)}
        </Text>
      </View>

      <Text style={styles.heading}>Nearby Capsules</Text>
      <Text style={styles.sub}>{capsules.length} capsule{capsules.length !== 1 ? 's' : ''} within range</Text>

      <ScrollView style={styles.list} showsVerticalScrollIndicator={false}>
        {capsules.length === 0 ? (
          <View style={styles.empty}>
            <Text style={styles.emptyIcon}>🗺️</Text>
            <Text style={styles.emptyText}>No capsules nearby yet.</Text>
            <Text style={styles.emptyHint}>Drop one and come back!</Text>
          </View>
        ) : (
          capsules.map(c => (
            <TouchableOpacity
              key={c.id}
              style={styles.card}
              onPress={() => navigation.navigate('CapsuleDetail', { capsule: c, currentLocation: location })}
            >
              <View style={styles.cardLeft}>
                <Text style={styles.cardIcon}>📦</Text>
              </View>
              <View style={styles.cardBody}>
                <Text style={styles.cardTitle}>Capsule nearby</Text>
                <Text style={styles.cardDist}>{Math.round(c.distanceMeters)} m away</Text>
                <Text style={styles.cardCoords}>
                  {c.latitude.toFixed(4)}, {c.longitude.toFixed(4)}
                </Text>
              </View>
              <Text style={styles.cardArrow}>›</Text>
            </TouchableOpacity>
          ))
        )}
      </ScrollView>

      <TouchableOpacity style={styles.refreshBtn} onPress={load}>
        <Text style={styles.refreshText}>{refreshing ? 'Refreshing…' : '🔄 Refresh'}</Text>
      </TouchableOpacity>
    </Screen>
  );
}

const styles = StyleSheet.create({
  heading: { fontSize: 26, fontWeight: '700', color: palette.text, marginBottom: 4 },
  sub: { fontSize: 13, color: palette.muted, marginBottom: 12 },
  msg: { fontSize: 16, color: palette.muted, textAlign: 'center', marginTop: 40 },
  locationBadge: {
    backgroundColor: palette.surfaceAlt,
    borderRadius: 8,
    padding: 8,
    marginBottom: 12,
  },
  locationText: { fontSize: 12, color: palette.muted, textAlign: 'center' },
  list: { flex: 1 },
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: palette.surface,
    borderRadius: 12,
    padding: 14,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: palette.border,
  },
  cardLeft: { marginRight: 12 },
  cardIcon: { fontSize: 28 },
  cardBody: { flex: 1 },
  cardTitle: { fontSize: 15, fontWeight: '600', color: palette.text },
  cardDist: { fontSize: 13, color: palette.primary, fontWeight: '700', marginTop: 2 },
  cardCoords: { fontSize: 11, color: palette.muted, marginTop: 2 },
  cardArrow: { fontSize: 22, color: palette.muted },
  empty: { alignItems: 'center', paddingTop: 60 },
  emptyIcon: { fontSize: 48, marginBottom: 12 },
  emptyText: { fontSize: 16, fontWeight: '600', color: palette.text },
  emptyHint: { fontSize: 13, color: palette.muted, marginTop: 4 },
  refreshBtn: {
    backgroundColor: palette.surfaceAlt,
    borderRadius: 12,
    padding: 12,
    alignItems: 'center',
    marginTop: 8,
  },
  refreshText: { fontSize: 14, fontWeight: '600', color: palette.primary },
});
