import React, { useState } from 'react';
import { Alert, Text } from 'react-native';
import Screen from '../components/Screen';
import { Button, Input, Label } from '../components/FormControls';
import { useAuth } from '../context/AuthContext';

export default function RegisterScreen() {
  const { register } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const submit = async () => {
    try {
      await register(email.trim(), password);
      Alert.alert('Account created', 'You can now use Momento.');
    } catch (e) {
      Alert.alert('Registration failed', e.message);
    }
  };

  return (
    <Screen>
      <Text style={{ fontSize: 28, fontWeight: '700' }}>Create account</Text>
      <Label>Email</Label>
      <Input autoCapitalize="none" keyboardType="email-address" value={email} onChangeText={setEmail} />
      <Label>Password</Label>
      <Input secureTextEntry value={password} onChangeText={setPassword} />
      <Button title="Register" onPress={submit} />
    </Screen>
  );
}
