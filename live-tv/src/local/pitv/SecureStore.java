package local.pitv;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
public final class SecureStore {
 private final SharedPreferences prefs;
 private final String alias="pitv.private.v1";
 public SecureStore(Context context){prefs=context.getSharedPreferences("encrypted_library",Context.MODE_PRIVATE);}
 private SecretKey key() throws Exception {
  KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);
  if(ks.containsAlias(alias))return (SecretKey)ks.getKey(alias,null);
  KeyGenerator gen=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
  gen.init(new KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build());
  return gen.generateKey();
 }
 public boolean contains(String name){return prefs.contains(name);}
 public void put(String name,String value)throws Exception {
  Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());c.updateAAD(name.getBytes("UTF-8"));
  String record=Base64.encodeToString(c.getIV(),Base64.NO_WRAP)+":"+Base64.encodeToString(c.doFinal(value.getBytes("UTF-8")),Base64.NO_WRAP);
  if(!prefs.edit().putString(name,record).commit())throw new java.io.IOException("Save failed");
 }
 public String get(String name)throws Exception {
  String record=prefs.getString(name,null);if(record==null)return null;
  String[] parts=record.split(":",-1);if(parts.length!=2)throw new java.io.IOException("Invalid saved data");
  Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(parts[0],Base64.NO_WRAP)));c.updateAAD(name.getBytes("UTF-8"));
  return new String(c.doFinal(Base64.decode(parts[1],Base64.NO_WRAP)),"UTF-8");
 }
 public void remove(String name){prefs.edit().remove(name).apply();}
}
