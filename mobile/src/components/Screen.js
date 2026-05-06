import React from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { KeyboardAvoidingView, Platform, ScrollView, StyleSheet, View } from 'react-native';
import { palette } from '../theme/palette';

/**
 * Base screen wrapper.
 *
 * Props:
 *   scroll  – (default true) wraps children in a ScrollView with keyboard-
 *             avoidance.  Pass false for map/camera screens that manage their
 *             own scroll.
 *   edges   – SafeAreaView edges; defaults to ['top','bottom'].
 */
export default function Screen({ children, scroll = true, edges = ['top', 'bottom'] }) {
  return (
    <SafeAreaView style={styles.safe} edges={edges}>
      {scroll ? (
        <KeyboardAvoidingView
          style={styles.flex}
          behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        >
          <ScrollView
            contentContainerStyle={styles.scroll}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            <View style={styles.inner}>{children}</View>
          </ScrollView>
        </KeyboardAvoidingView>
      ) : (
        <View style={styles.noScrollInner}>{children}</View>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: palette.bg },
  flex: { flex: 1 },
  scroll: { flexGrow: 1, padding: 16 },
  inner: { flex: 1, gap: 16 },
  noScrollInner: { flex: 1, padding: 16, gap: 16 },
});
