/*
 * Handle SHA-1 ID management for reporting DNC violations on Android.
 * NOT USED!
 * 
 * Copyright (C) 2011, Raj Mathur <raju@kandalaya.org>
 * 
 * This file is part of DNCViolation.
 * 
 * DNCViolation is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DNCViolation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DNCViolation.  If not, see <http://www.gnu.org/licenses/>.
 */
package kandalaya.dncviolation;

import java.security.*;

import android.util.Log;

public class DNCViolationID
{
  /**
   * Create an SHA-1 message digest of the given text.
   * 
   * Shamelessly stolen from:
   * http://blog.ryanrampersad.com/2009/02/26/hex-sha1-hashes-in-java/
   * 
   * @param text String to be digested
   * @return byte[] array containing digest
   */
  private static byte[] createHash(String text)
  {
    try
    {
        byte[] b = text.getBytes();
        MessageDigest
          algorithm = MessageDigest.getInstance("SHA-1");
        algorithm.reset();
        algorithm.update(b);
        byte messageDigest[] = algorithm.digest();
        return( messageDigest );
    }
    catch(NoSuchAlgorithmException nsae)
    {
        Log.d("DNCViolation", "no such algorithm exception");
        return null;
    }
  }
  /**
   * Convert the given byte[] array to a hex string.
   * 
   * Shamelessly stolen from:
   * http://blog.ryanrampersad.com/2009/02/26/hex-sha1-hashes-in-java/
   * 
   * @param b array containing values to be converted
   * @return String containing hex representation
   */
  private static String asHex(byte[] b)
  {
    String
      result = "";
    for( int i = 0; i < b.length; i++)
    {
        result += Integer.toString(( b[i] & 0xff ) + 0x100, 16).substring(1);
    }
    return( result );
  }
  /**
   * String from which digest was made.
   */
  String
    text;
  /**
   * Hash of the text.
   */
  byte[]
    hash;
  /**
   * Hex format if user has ever asked for it.
   */
  String
    hex;
  /**
   * Construct a hash ID of the given string.  Store string for later comparisons.
   * @param text string to be digested into an ID.
   */
  public DNCViolationID( String text )
  {
    this.text = text;
    this.hash = createHash(text);
    this.hex = asHex(hash);
    return;
  }
  /**
   * @return the text
   */
  public String getText()
  {
    return text;
  }
  /**
   * @return the hash
   */
  public byte[] getHash()
  {
    return hash;
  }
  /**
   * @return the hex
   */
  public String getHex()
  {
    return hex;
  }
  /**
   * Compare two IDs.
   * 
   * Currently we compare on the original string (for our purposes it should be the fastest).
   * 
   * @param id the ID to compare with
   * @return whether the two IDs are identical or not
   */
  public boolean equals( DNCViolationID id )
  {
    return( this.getText().equals(id.getText()) );
  }
  /**
   * Convert to the hex form.
   */
  @Override
  public String toString()
  {
    return( this.hex );
  }
}
