import React, { useEffect, useState } from 'react';
import { Alert, Text, View } from 'react-native';
import Screen from '../components/Screen';
import { Button } from '../components/FormControls';
import { deleteCapsule, getMyCapsules } from '../api/capsules';

export default function DroppedCapsulesScreen() {
  const [items, setItems] = useState([]);
  const load = () => getMyCapsules().then(({ data }) => setItems(data));
  useEffect(() => { load(); }, []);

  const remove = async id => {
    await deleteCapsule(id);
    Alert.alert('Deleted', 'Capsule removed.');
    load();
  };

  return (
    <Screen>
      <Text style={{ fontSize: 28, fontWeight: '700' }}>My capsules</Text>
      {items.map(item => (
        <View key={item.id} style={{ gap: 8 }}>
          <Text>{item.status} · expires {item.expiryAt}</Text>
          <Button title="Delete" kind="secondary" onPress={() => remove(item.id)} />
        </View>
      ))}
    </Screen>
  );
}
