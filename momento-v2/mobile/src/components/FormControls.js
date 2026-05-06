import React from 'react';
import {
  ActivityIndicator,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
} from 'react-native';
import { palette } from '../theme/palette';

export function Label({ children }) {
  return <Text style={styles.label}>{children}</Text>;
}

export function Input(props) {
  return (
    <TextInput
      placeholderTextColor={palette.muted}
      style={[styles.input, props.multiline && styles.inputMultiline]}
      {...props}
    />
  );
}

/**
 * Button component.
 *
 * Props:
 *   title    – button label
 *   onPress  – handler
 *   kind     – 'primary' (default) | 'secondary' | 'danger'
 *   loading  – show spinner and disable interaction
 *   disabled – disable interaction
 */
export function Button({ title, onPress, kind = 'primary', loading = false, disabled = false }) {
  const isDisabled = disabled || loading;
  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={isDisabled}
      style={[
        styles.button,
        kind === 'secondary' && styles.secondary,
        kind === 'danger' && styles.danger,
        isDisabled && styles.disabled,
      ]}
      activeOpacity={0.75}
    >
      {loading ? (
        <ActivityIndicator
          color={kind === 'primary' || kind === 'danger' ? '#fff' : palette.text}
          size="small"
        />
      ) : (
        <Text
          style={[
            styles.buttonText,
            kind === 'secondary' && styles.secondaryText,
            kind === 'danger' && styles.dangerText,
          ]}
        >
          {title}
        </Text>
      )}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  label: { fontSize: 14, fontWeight: '600', color: palette.text },
  input: {
    borderWidth: 1,
    borderColor: palette.border,
    borderRadius: 12,
    padding: 14,
    backgroundColor: '#fff',
    fontSize: 15,
    color: palette.text,
  },
  inputMultiline: {
    minHeight: 100,
    textAlignVertical: 'top',
  },
  button: {
    backgroundColor: palette.primary,
    borderRadius: 12,
    padding: 14,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 50,
  },
  secondary: { backgroundColor: palette.surfaceAlt },
  danger: { backgroundColor: palette.error },
  disabled: { opacity: 0.5 },
  buttonText: { color: '#fff', fontWeight: '700', fontSize: 15 },
  secondaryText: { color: palette.text },
  dangerText: { color: '#fff' },
});
