import React, { useEffect, useState } from 'react';
import { Text, View } from 'react-native';
import Screen from '../components/Screen';
import { getHistory } from '../api/capsules';

export default function HistoryScreen() {
  const [items, setItems] = useState([]);
  useEffect(() => { getHistory().then(({ data }) => setItems(data)); }, []);
  return (
    <Screen>
      <Text style={{ fontSize: 28, fontWeight: '700' }}>Discovery history</Text>
      {items.map(item => <View key={item.discoveryId}><Text>{item.discoveredAt} · +{item.pointsAwarded} points</Text></View>)}
    </Screen>
  );
}
