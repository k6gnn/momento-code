import React, { useEffect, useState } from 'react';
import { Text } from 'react-native';
import Screen from '../components/Screen';
import { Button } from '../components/FormControls';
import { getProfile } from '../api/capsules';
import { useAuth } from '../context/AuthContext';

export default function ProfileScreen() {
  const { logout } = useAuth();
  const [profile, setProfile] = useState(null);
  useEffect(() => { getProfile().then(({ data }) => setProfile(data)); }, []);
  return (
    <Screen>
      <Text style={{ fontSize: 28, fontWeight: '700' }}>Profile</Text>
      <Text>Username: {profile?.username || 'New user'}</Text>
      <Text>Points: {profile?.pointsTotal || 0}</Text>
      <Text>Dropped: {profile?.droppedCount || 0}</Text>
      <Text>Discovered: {profile?.discoveredCount || 0}</Text>
      <Button title="Sign out" kind="secondary" onPress={logout} />
    </Screen>
  );
}
