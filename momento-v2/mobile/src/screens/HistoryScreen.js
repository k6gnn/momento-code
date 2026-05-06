import React, { useCallback, useEffect, useState } from 'react';
import { Alert, RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';
import Screen from '../components/Screen';
import { getHistory } from '../api/capsules';
import { palette } from '../theme/palette';

export default function HistoryScreen() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async (showRefresh = false) => {
    showRefresh ? setRefreshing(true) : setLoading(true);
    try {
      const { data } = await getHistory();
      setItems(data);
    } catch (e) {
      Alert.alert('Could not load history', e.message);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    load(false);
  }, [load]);

  if (loading) {
    return (
      <Screen>
        <Text style={styles.msg}>Loading history…</Text>
      </Screen>
    );
  }

  return (
    <Screen scroll={false}>
      <Text style={styles.heading}>Discovery history</Text>
      <ScrollView
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={() => load(true)}
            tintColor={palette.primary}
            colors={[palette.primary]}
          />
        }
      >
        {items.length === 0 ? (
          <View style={styles.empty}>
            <Text style={styles.emptyIcon}>🔍</Text>
            <Text style={styles.emptyText}>No discoveries yet.</Text>
            <Text style={styles.emptyHint}>Find capsules near you on the Map tab!</Text>
          </View>
        ) : (
          items.map(item => (
            <View key={item.discoveryId} style={styles.card}>
              <Text style={styles.date}>{item.discoveredAt}</Text>
              <Text style={styles.points}>+{item.pointsAwarded} pts</Text>
            </View>
          ))
        )}
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  heading: { fontSize: 28, fontWeight: '700', color: palette.text, marginBottom: 12 },
  msg: { fontSize: 16, color: palette.muted, textAlign: 'center', marginTop: 40 },
  card: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: palette.surface,
    borderRadius: 12,
    padding: 14,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: palette.border,
  },
  date: { fontSize: 14, color: palette.text },
  points: { fontSize: 14, fontWeight: '700', color: palette.success },
  empty: { alignItems: 'center', paddingTop: 60 },
  emptyIcon: { fontSize: 48, marginBottom: 12 },
  emptyText: { fontSize: 16, fontWeight: '600', color: palette.text },
  emptyHint: { fontSize: 13, color: palette.muted, marginTop: 4 },
});
