/**
 * (C) Copyright IBM Corporation 2014, 2015, 2016.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wasdev.wlp.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import net.wasdev.wlp.deploy.*;

/**
 * deploy ant tasks
 */
public class DeployTask extends AbstractTask {

    protected static final String START_APP_MESSAGE_CODE_REG = "CWWKZ0001I.*";

    private static final long APP_START_TIMEOUT_DEFAULT = 30 * 1000;

    private final List<FileSet> fileSets = new ArrayList<FileSet>();
    private File appFile;
    private String deployName;
    private String deployDestination = "dropins";
    private long appStartTimeout;

    private String timeout;
    private String contextRoot;
    private String commonLibraryRef;

    @Override
    public void execute() {

        super.initTask();

        // Check for no arguments
        if ((appFile == null) && (fileSets.size() == 0)) {
            throw new BuildException(getMessage("error.fileset.set"), getLocation());
        }
        
        if (!deployDestination.equals("dropins") && !deployDestination.equals("configDropins")) {
            throw new BuildException(getMessage("error.parameter.value.invalid"));
        }
        
        if (deployName != null && appFile == null) {
            throw new BuildException(getMessage("error.file.set"), getLocation());
        }
        
        if (deployDestination.equals("dropins") && (contextRoot != null || commonLibraryRef != null)) {
            throw new BuildException(getMessage("error.deploy.dropins.invalid.parameter"), getLocation());
        }
        if (!deployDestination.equals("dropins") && deployName != null) {
            throw new BuildException(getMessage("error.deploy.configdropins.invalid.parameter"), getLocation());
        }

        appStartTimeout = APP_START_TIMEOUT_DEFAULT;
        if (timeout != null && !timeout.equals("")) {
            appStartTimeout = Long.valueOf(timeout);
        }
        
        if (deployDestination.equals("dropins")) {
            deployToDropins();
        } else {
            deployToConfigDropins();
        }
        
    }
    
    private void deployToDropins() {
    	File dropInFolder = new File(serverConfigDir, "dropins");
    	
    	DeployTypes deploy=new DeployTypes();
    	deploy.setServerOutputDir(serverOutputDir);
    	deploy.setAppStartTimeout(appStartTimeout);
    	
        // deploy app specified as a file
        if (checkAppFile(appFile)) {
            File destFile = new File(dropInFolder, deployName == null ? appFile.getName() : deployName);
            deploy.dropins(appFile, destFile);
        }
        // deploy apps specified as fileSets
        List<File> files = scanFileSets();
        for (File file : files) {
            File destFile = new File(dropInFolder, file.getName());
            deploy.dropins(file, destFile);
        }
    }

    private void deployToConfigDropins() {
        File overridesFolder = new File(serverConfigDir, "configDropins/overrides");
        
        DeployTypes deploy=new DeployTypes();
        deploy.setServerOutputDir(serverOutputDir);
        deploy.setAppStartTimeout(appStartTimeout);
        deploy.setCommonLibraryRef(commonLibraryRef);
        deploy.setContextRoot(contextRoot);
        
        if (!overridesFolder.exists()) {
            if (!overridesFolder.mkdirs()) {
                throw new BuildException(getMessage("error.deploy.fail", overridesFolder.getPath()));
            }
        }
        
        if (checkAppFile(appFile)) {
            deploy.configDropins(appFile, overridesFolder);
        }
        
        List<File> files = scanFileSets();
        for (File app : files) {
        	deploy.configDropins(app, overridesFolder);
        }
    }

    public boolean checkAppFile(File file) {
    	if (file != null) {
            if (!file.exists()) {
                throw new BuildException(getMessage("error.deploy.file.noexist", file), getLocation());
            } else if (file.isDirectory()) {
                throw new BuildException(getMessage("error.deploy.file.isdirectory", file), getLocation());
            }
            return true;
        }
    	return false;
    }

    /**
     * returns the list of files (full path name) to process.
     *
     * @return the list of files included via the filesets.
     */
    private List<File> scanFileSets() {
        final List<File> list = new ArrayList<File>();

        for (int i = 0; i < fileSets.size(); i++) {
            final FileSet fs = fileSets.get(i);
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            ds.scan();

            final String[] names = ds.getIncludedFiles();

            //Throw a BuildException if the directory specified as parameter is empty.
            if (names.length == 0) {
                throw new BuildException(getMessage("error.deploy.fileset.invalid"), getLocation());
            }

            for (String element : names) {
                list.add(new File(ds.getBasedir(), element));
            }
        }

        return list;
    }

    /**
     * Adds a set of files (nested fileset attribute).
     *
     * @param fs the file set to add
     */
    public void addFileset(FileSet fs) {
        fileSets.add(fs);
    }

    public void setFile(File appFile) {
        this.appFile = appFile;
    }

    /**
     * @return the timeout
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    /**
     * @return the deployName
     */
    public String getDeployName() {
        return deployName;
    }

    /**
     * @param name the deployName to set
     */
    public void setDeployName(String name) {
        emptyParameter(name, "deployName");
        this.deployName = name;
    }
    
    /**
     * @return the deploy destination
     */
    public String getDeployDestination() {
        return deployDestination;
    }

    /**
     * @param deployDestination The deploy destination to set
     */
    public void setDeployDestination(String deployDestination) {
    	emptyParameter(deployDestination, "deployDestination");
        this.deployDestination = deployDestination;
    }

    /**
     * @return the contextRoot
     */
    public String contextRoot() {
        return contextRoot;
    }

    /**
     * @param contextRoot the contextRoot to set
     */
    public void setContextRoot(String contextRoot) {
    	emptyParameter(contextRoot, "contextRoot");
        this.contextRoot = contextRoot;
    }

    /**
     * @return the commonLibraryRef
     */
    public String getCommonLibraryRef() {
        return commonLibraryRef;
    }

    /**
     * @param commonLibraryRef the commonLibraryRef to set
     */
    public void setCommonLibraryRef(String commomLibraryRef) {
        emptyParameter(commonLibraryRef, "commonLibraryRef");
        this.commonLibraryRef = commomLibraryRef;
    }

}
