/*******************************************************************************
 * Pentaho Data Science
 * <p/>
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 * <p/>
 * ******************************************************************************
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 * <p/>
 ******************************************************************************/

package org.pentaho.di.trans.steps.pmi;

import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import weka.core.Version;
import weka.core.WekaPackageManager;
import weka.core.packageManagement.VersionPackageConstraint;

import java.util.List;

/**
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: $
 */
@KettleLifecyclePlugin( id = "PMILifecycleListener", name = "PMIScoringLifecycleListener" )
public class PMILifecycleListener implements KettleLifecycleListener {
  @Override public void onEnvironmentInit() throws LifecycleException {
    System.setProperty( "weka.core.logging.Logger", "weka.core.logging.ConsoleLogger" );

    // TODO replace this by some code that somehow locates the pdm jar file in plugins/steps/pmi/lib
    // This allows the Spark engine to locate the main weka.jar file for use in the Spark execution environment
    //System.setProperty( "weka.jar.filename", "pdm-ce-3.8.3.1.jar" );
    System.setProperty( "weka.jar.filename", "weka-stable-3.8.5.jar" );

    // check that the required packages are installed (and possibly install if not)
    try {
      // make sure the package metadata cache is established first
      //WekaPackageManager.establishCacheIfNeeded( System.out );
      //WekaPackageManager.checkForNewPackages( System.out );
      //String thisName = System.getProperty( "os.name" );

      // python dependency
      weka.core.packageManagement.Package
          pythonPackage =
          weka.core.WekaPackageManager.getInstalledPackageInfo( "wekaPython" );
      if ( pythonPackage == null ) {
        String latestCompatibleVersion = getLatestVersion( "wekaPython" );
        System.out.println(
            "[PMI] wekaPython package is not installed - attempting to install version " + latestCompatibleVersion );
        WekaPackageManager.installPackageFromRepository( "wekaPython", latestCompatibleVersion, System.out );
      }

      // R dependency
      weka.core.packageManagement.Package rPackage = weka.core.WekaPackageManager.getInstalledPackageInfo( "RPlugin" );
      if ( rPackage == null ) {
        String latestCompatibleVersion = getLatestVersion( "RPlugin" );
        System.out.println(
            "[PMI] RPlugin package is not installed - attempting to install version " + latestCompatibleVersion );
        WekaPackageManager.installPackageFromRepository( "RPlugin", latestCompatibleVersion, System.out );
      }

      // Weka dependency
      weka.core.packageManagement.Package
          libSVMPackage =
          weka.core.WekaPackageManager.getInstalledPackageInfo( "LibSVM" );
      if ( libSVMPackage == null ) {
        String latestCompatibleVersion = getLatestVersion( "LibSVM" );
        System.out.println(
            "[PMI] libSVM package is not installed - attempting to install version " + latestCompatibleVersion );
        WekaPackageManager.installPackageFromRepository( "LibSVM", latestCompatibleVersion, System.out );
      }

      // MLlib dependencies
      weka.core.packageManagement.Package
          distributedWekaBasePackage =
          weka.core.WekaPackageManager.getInstalledPackageInfo( "distributedWekaBase" );
      if ( distributedWekaBasePackage == null ) {
        String latestCompatibleVersion = getLatestVersion( "distributedWekaBase" );
        System.out.println( "[PMI] distributed Weka base package is not installed - attempting to install version "
            + latestCompatibleVersion );
        WekaPackageManager.installPackageFromRepository( "distributedWekaBase", latestCompatibleVersion, System.out );
      }

      weka.core.packageManagement.Package
          distributedWekaSparkDevPackage =
          weka.core.WekaPackageManager.getInstalledPackageInfo( "distributedWekaSpark2Dev" );
      if ( distributedWekaSparkDevPackage == null ) {
        String latestCompatibleVersion = getLatestVersion( "distributedWekaSpark2Dev" );
        System.out.println( "[PMI] distributed Weka Spark package is not installed - attempting to install version "
            + latestCompatibleVersion );
        WekaPackageManager
            .installPackageFromRepository( "distributedWekaSpark2Dev", latestCompatibleVersion, System.out );
      }

      // see if we can install a netlib native package to speed up linear regression in Weka.
      /*if ( thisName.toLowerCase().contains( "mac" ) ) {
        weka.core.packageManagement.Package
            netlibNative =
            weka.core.WekaPackageManager.getInstalledPackageInfo( "netlibNativeOSX" );
        if ( netlibNative == null ) {
          String latestCompatibleVersion = getLatestVersion( "netlibNativeOSX" );
          System.out.println( "[PMI] netlibNativeOSX package is not installed - attempting to install version "
              + latestCompatibleVersion );
          weka.core.WekaPackageManager
              .installPackageFromRepository( "netlibNativeOSX", latestCompatibleVersion, System.out );
        }
      } else if ( thisName.toLowerCase().contains( "win" ) ) {
        weka.core.packageManagement.Package
            netlibNative =
            weka.core.WekaPackageManager.getInstalledPackageInfo( "netlibNativeWindows" );
        if ( netlibNative == null ) {
          String latestCompatibleVersion = getLatestVersion( "netlibNativeWindows" );
          System.out.println( "[PMI] netlibNativeWindows package is not installed - attempting to install version "
              + latestCompatibleVersion );
          weka.core.WekaPackageManager
              .installPackageFromRepository( "netlibNativeWindows", latestCompatibleVersion, System.out );
        }
      } else if ( thisName.toLowerCase().contains( "linux" ) ) {
        weka.core.packageManagement.Package
            netlibNative =
            weka.core.WekaPackageManager.getInstalledPackageInfo( "netlibNativeLinux" );
        if ( netlibNative == null ) {
          String latestCompatibleVersion = getLatestVersion( "netlibNativeLinux" );
          System.out.println( "[PMI] netlibNativeLinux package is not installed - attempting to install version "
              + latestCompatibleVersion );
          weka.core.WekaPackageManager
              .installPackageFromRepository( "netlibNativeLinux", latestCompatibleVersion, System.out );
        }
      } */

      // DL4j dependency. The latest version of wekaDl4j comes with CPU support and instructions on how to
      // install GPU support
      weka.core.packageManagement.Package
          dl4jPackage =
          weka.core.WekaPackageManager.getInstalledPackageInfo( "wekaDeeplearning4j" );
      if ( dl4jPackage == null ) {
        String latestCompatibleVersion = getLatestVersion( "wekaDeeplearning4j" );
        System.out.println( "[PMI] wekaDeeplearning4j package is not installed - attempting to install version "
            + latestCompatibleVersion );
        weka.core.WekaPackageManager
            .installPackageFromRepository( "wekaDeeplearning4j", latestCompatibleVersion, System.out );
      }

      // TODO
      weka.core.packageManagement.Package
          kerasZooPackage =
          weka.core.WekaPackageManager.getInstalledPackageInfo( "kerasZoo" );
      if ( kerasZooPackage == null ) {
        String latestCompatibleVersion = getLatestVersion( "kerasZoo" );
        System.out.println(
            "[PMI] kerasZoo package is not installed - attempting to install version " + latestCompatibleVersion );
        weka.core.WekaPackageManager.installPackageFromRepository( "kerasZoo", latestCompatibleVersion, System.out );
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    weka.core.WekaPackageManager.loadPackages( false );
  }

  @Override public void onEnvironmentShutdown() {
    // Noop
  }

  protected String getLatestVersion( String packageName ) throws Exception {
    String version = "none";
    List<Object> availableVersions = WekaPackageManager.getRepositoryPackageVersions( packageName );
    // version numbers will be in descending sorted order from the
    // repository. We want the most recent version that is compatible
    // with the base weka install
    for ( Object v : availableVersions ) {
      weka.core.packageManagement.Package
          versionedPackage =
          WekaPackageManager.getRepositoryPackageInfo( packageName, v.toString() );
      if ( versionedPackage.isCompatibleBaseSystem() ) {
        version = versionedPackage.getPackageMetaDataElement( VersionPackageConstraint.VERSION_KEY ).toString();
        break;
      }
    }

    if ( version.equals( "none" ) ) {
      throw new Exception(
          "Was unable to find a version of '" + packageName + "' that is compatible with Weka " + Version.VERSION );
    }

    return version;
  }
}
