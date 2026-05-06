import React, { useCallback, useEffect, useState } from 'react';
import { Alert, RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';
import Screen from '../components/Screen';
import { Button } from '../components/FormControls';
import { getProfile } from '../api/capsules';
import { useAuth } from '../context/AuthContext';
import { palette } from '../theme/palette';

export default function ProfileScreen() {
  const { logout, user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [signingOut, setSigningOut] = useState(false);

  const load = useCallback(async (showRefresh = false) => {
    showRefresh ? setRefreshing(true) : setLoading(true);
    try {
      const { data } = await getProfile();
      setProfile(data);
    } catch (e) {
      Alert.alert('Could not load profile', e.message);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    load(false);
  }, [load]);

  const handleLogout = async () => {
    Alert.alert('Sign out', 'Are you sure you want to sign out?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Sign out',
        style: 'destructive',
        onPress: async () => {
          setSigningOut(true);
          try {
            await logout();
          } catch (e) {
            Alert.alert('Sign out failed', e.message);
            setSigningOut(false);
          }
        },
      },
    ]);
  };

  if (loading) {
    return (
      <Screen>
        <Text style={styles.msg}>Loading profile…</Text>
      </Screen>
    );
  }

  return (
    <Screen scroll={false}>
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
        <Text style={styles.heading}>Profile</Text>

        <View style={styles.card}>
          <StatRow label="Username" value={profile?.username || user?.email || 'New user'} />
          <StatRow label="Points" value={profile?.pointsTotal ?? 0} />
          <StatRow label="Dropped" value={profile?.droppedCount ?? 0} />
          <StatRow label="Discovered" value={profile?.discoveredCount ?? 0} />
        </View>

        <Button
          title="Sign out"
          kind="secondary"
          onPress={handleLogout}
          loading={signingOut}
        />
      </ScrollView>
    </Screen>
  );
}

function StatRow({ label, value }) {
  return (
    <View style={styles.statRow}>
      <Text style={styles.statLabel}>{label}</Text>
      <Text style={styles.statValue}>{String(value)}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  heading: { fontSize: 28, fontWeight: '700', color: palette.text, marginBottom: 16 },
  msg: { fontSize: 16, color: palette.muted, textAlign: 'center', marginTop: 40 },
  card: {
    backgroundColor: palette.surface,
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: palette.border,
    gap: 12,
  },
  statRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  statLabel: { fontSize: 14, color: palette.muted },
  statValue: { fontSize: 15, fontWeight: '600', color: palette.text },
});
