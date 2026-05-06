import React from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScrollView, StyleSheet, View } from 'react-native';
import { palette } from '../theme/palette';

export default function Screen({ children, scroll = true }) {
  const content = <View style={styles.inner}>{children}</View>;
  return (
    <SafeAreaView style={styles.safe}>
      {scroll ? <ScrollView contentContainerStyle={styles.scroll}>{content}</ScrollView> : content}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: palette.bg },
  scroll: { padding: 16 },
  inner: { flex: 1, padding: 16, gap: 16 }
});
