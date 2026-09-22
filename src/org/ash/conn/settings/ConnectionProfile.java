/*
 *-------------------
 * The ConnectionProfile.java is part of ASH Viewer
 *-------------------
 * 
 * ASH Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASH Viewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ASH Viewer.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2009, Alex Kardapolov, All rights reserved.
 *
 */
package org.ash.conn.settings;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.ash.util.Options;

/** Swing adapter for password-free profile persistence. */
public class ConnectionProfile {
  public void loadProfile(JFrame parent, File file, ArrayList conns, Vector connNames) {
    try {
      DbConnection connection = ProfileStore.read(file.toPath());
      conns.add(connection);
      connNames.add(connection.getName());
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(parent, ex.getMessage(),
          Options.getInstance().getResource("error on loading connections profile files."),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  public void saveProfile(JFrame parent, DbConnection connection, boolean isEdit) {
    try {
      ProfileStore.write(Path.of("profile"), connection);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(parent, ex.getMessage(),
          Options.getInstance().getResource("error on saving connections profile files."),
          JOptionPane.ERROR_MESSAGE);
      throw new IllegalStateException("Connection profile could not be saved", ex);
    }
  }
}
