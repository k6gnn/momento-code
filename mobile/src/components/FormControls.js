import React from 'react';
import { StyleSheet, Text, TextInput, TouchableOpacity } from 'react-native';
import { palette } from '../theme/palette';

export function Label({ children }) {
  return <Text style={styles.label}>{children}</Text>;
}

export function Input(props) {
  return <TextInput placeholderTextColor={palette.muted} style={styles.input} {...props} />;
}

export function Button({ title, onPress, kind = 'primary' }) {
  return (
    <TouchableOpacity onPress={onPress} style={[styles.button, kind === 'secondary' && styles.secondary]}>
      <Text style={[styles.buttonText, kind === 'secondary' && styles.secondaryText]}>{title}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  label: { fontSize: 14, fontWeight: '600', color: palette.text },
  input: { borderWidth: 1, borderColor: palette.border, borderRadius: 12, padding: 14, backgroundColor: '#fff' },
  button: { backgroundColor: palette.primary, borderRadius: 12, padding: 14, alignItems: 'center' },
  secondary: { backgroundColor: palette.surfaceAlt },
  buttonText: { color: '#fff', fontWeight: '700' },
  secondaryText: { color: palette.text }
});
