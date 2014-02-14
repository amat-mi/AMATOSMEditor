package org.openstreetmap.josm.plugins.amatosmeditor;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class AMATOSMEditorPlugin extends Plugin {
   LaunchAction action;

   /**
    * constructor
    */
   public AMATOSMEditorPlugin(PluginInformation info) {
      super(info);
      action = new LaunchAction(getPluginDir());
      MainMenu.add(Main.main.menu.dataMenu, action, false,0);
   }
}
