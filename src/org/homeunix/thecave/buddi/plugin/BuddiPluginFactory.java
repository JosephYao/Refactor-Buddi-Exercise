/*
 * Created on Aug 12, 2007 by wyatt
 */
package org.homeunix.thecave.buddi.plugin;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;

import org.homeunix.thecave.buddi.Buddi;
import org.homeunix.thecave.buddi.Const;
import org.homeunix.thecave.buddi.plugin.api.BuddiExportPlugin;
import org.homeunix.thecave.buddi.plugin.api.BuddiImportPlugin;
import org.homeunix.thecave.buddi.plugin.api.BuddiPreferencePlugin;
import org.homeunix.thecave.buddi.plugin.api.BuddiReportPlugin;
import org.homeunix.thecave.buddi.plugin.api.BuddiRunnablePlugin;
import org.homeunix.thecave.buddi.view.dialogs.prefs.AdvancedPreferences;
import org.homeunix.thecave.buddi.view.dialogs.prefs.LocalePreferences;
import org.homeunix.thecave.buddi.view.dialogs.prefs.NetworkPreferences;
import org.homeunix.thecave.buddi.view.dialogs.prefs.PluginPreferences;
import org.homeunix.thecave.buddi.view.dialogs.prefs.ViewPreferences;
import org.homeunix.thecave.moss.plugin.MossPlugin;
import org.homeunix.thecave.moss.plugin.factory.PluginFactory;

public class BuddiPluginFactory extends PluginFactory {

	public static final FileFilter pluginFilter = new FileFilter(){
		public boolean accept(File pathname) {
			if (pathname.getName().endsWith(Const.PLUGIN_EXTENSION)
					&& pathname.isFile())
				return true;
			return false;
		}
	};

	/**
	 * Returns a list of valid plugin objects, both built-in and user-defined.
	 * This method returns only plugins of type BuddiReportPlugin. 
	 * @return
	 */
	public static List<BuddiReportPlugin> getReportPlugins(){
		List<BuddiReportPlugin> reports = new LinkedList<BuddiReportPlugin>();

		//Load built in plugins
		for (String className : Const.BUILT_IN_REPORTS){
			MossPlugin plugin = BuddiPluginFactory.getValidPluginFromClasspath(className);
			if (plugin instanceof BuddiReportPlugin){
				reports.add((BuddiReportPlugin) plugin);
			}
		}

		//Load user defined plugins
		File[] plugins = Buddi.getPluginsFolder().listFiles(pluginFilter); 
		if (plugins != null){
			for (File pluginFile : plugins){
				for (MossPlugin plugin : getMossPluginsFromJar(pluginFile, Const.VERSION)) {
					if (plugin instanceof BuddiReportPlugin){
						reports.add((BuddiReportPlugin) plugin);
					}
				}
			}
		}

		return reports;
	}

	/**
	 * Returns a list of valid plugin objects, both built-in and user-defined.
	 * This method returns only plugins of type BuddiPreferencePlugin.
	 * @return
	 */
	public static List<BuddiPreferencePlugin> getPreferencePlugins(){
		List<BuddiPreferencePlugin> preferences = new LinkedList<BuddiPreferencePlugin>();

		//Load built in plugins.  We kinda cheat for these plugins; instead of
		// loading them reflexivly, we just create new objects.
		preferences.add(new ViewPreferences());
		preferences.add(new PluginPreferences());
		preferences.add(new LocalePreferences());
		preferences.add(new NetworkPreferences());
		preferences.add(new AdvancedPreferences());

		File[] plugins = Buddi.getPluginsFolder().listFiles(pluginFilter); 
		if (plugins != null){
			for (File pluginFile : plugins){
				for (MossPlugin plugin : getMossPluginsFromJar(pluginFile, Const.VERSION)) {
					if (plugin instanceof BuddiPreferencePlugin){
						preferences.add((BuddiPreferencePlugin) plugin);
					}
				}
			}
		}

		return preferences;
	}
	
	public static List<File> getPluginFiles(){
		List<File> pluginFiles = new LinkedList<File>();
		
		//Loop through all files fuond in the plugins folder, and see if they contain a valid
		// plugin.  If so, add it to the list.  This time (Buddi 3), we focus on the plugin file
		// itself, rather than each plugin class within it.
		File[] plugins = Buddi.getPluginsFolder().listFiles(pluginFilter); 
		if (plugins != null){
			for (File pluginFile : plugins){
				for (MossPlugin plugin : getMossPluginsFromJar(pluginFile, Const.VERSION)) {
					if (plugin instanceof BuddiExportPlugin
							|| plugin instanceof BuddiImportPlugin
							|| plugin instanceof BuddiPreferencePlugin
							|| plugin instanceof BuddiReportPlugin
							|| plugin instanceof BuddiRunnablePlugin){
						pluginFiles.add(pluginFile);
						break;
					}
				}
			}
		}
		
		return pluginFiles;
	}
}
