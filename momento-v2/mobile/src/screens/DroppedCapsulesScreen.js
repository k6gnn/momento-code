import React, { useCallback, useEffect, useState } from 'react';
import { Alert, RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';
import Screen from '../components/Screen';
import { Button } from '../components/FormControls';
import { deleteCapsule, getMyCapsules } from '../api/capsules';
import { palette } from '../theme/palette';

export default function DroppedCapsulesScreen() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [deletingId, setDeletingId] = useState(null);

  const load = useCallback(async (showRefresh = false) => {
    showRefresh ? setRefreshing(true) : setLoading(true);
    try {
      const { data } = await getMyCapsules();
      setItems(data);
    } catch (e) {
      Alert.alert('Could not load capsules', e.message);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    load(false);
  }, [load]);

  const remove = (id) => {
    Alert.alert(
      'Delete capsule',
      'This cannot be undone. Are you sure?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            setDeletingId(id);
            try {
              await deleteCapsule(id);
              setItems(prev => prev.filter(i => i.id !== id));
            } catch (e) {
              Alert.alert('Delete failed', e.message);
            } finally {
              setDeletingId(null);
            }
          },
        },
      ],
    );
  };

  if (loading) {
    return (
      <Screen>
        <Text style={styles.msg}>Loading your capsules…</Text>
      </Screen>
    );
  }

  return (
    <Screen scroll={false}>
      <Text style={styles.heading}>My capsules</Text>
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
            <Text style={styles.emptyIcon}>📭</Text>
            <Text style={styles.emptyText}>No capsules dropped yet.</Text>
          </View>
        ) : (
          items.map(item => (
            <View key={item.id} style={styles.card}>
              <View style={styles.cardInfo}>
                <Text style={styles.status}>{item.status}</Text>
                <Text style={styles.expiry}>Expires: {item.expiryAt}</Text>
              </View>
              <Button
                title="Delete"
                kind="danger"
                loading={deletingId === item.id}
                disabled={deletingId !== null}
                onPress={() => remove(item.id)}
              />
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
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: palette.surface,
    borderRadius: 12,
    padding: 14,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: palette.border,
    gap: 8,
  },
  cardInfo: { flex: 1 },
  status: { fontSize: 14, fontWeight: '600', color: palette.text, textTransform: 'capitalize' },
  expiry: { fontSize: 12, color: palette.muted, marginTop: 2 },
  empty: { alignItems: 'center', paddingTop: 60 },
  emptyIcon: { fontSize: 48, marginBottom: 12 },
  emptyText: { fontSize: 16, color: palette.muted },
});
