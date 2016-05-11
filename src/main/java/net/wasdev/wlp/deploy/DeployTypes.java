/**
 * (C) Copyright IBM Corporation 2016.
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
package net.wasdev.wlp.deploy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;

import net.wasdev.wlp.ant.DeployTask;

public class DeployTypes extends DeployTask{
	private long appStartTimeout;
	private String commonLibraryRef;
	private String contextRoot;

    public void setServerOutputDir(File serverOutputDir) {
        this.serverOutputDir = serverOutputDir;
    }
    
    public File getServerOutputDir() {
        return serverOutputDir;
    }
    
    public void setAppStartTimeout(long appStartTimeout) {
        this.appStartTimeout = appStartTimeout;
    }
    
    public long getAppStartTimeout() {
        return appStartTimeout;
    }
    
    public void commonLibraryRef(String commonLibraryRef) {
        this.commonLibraryRef = commonLibraryRef;
    }
    
    public String getCommonLibraryRef() {
        return commonLibraryRef;
    }
    
    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }
    
    public String getContextRoot() {
        return contextRoot;
    }

    public void configDropins(File app, File overridesFolder) {
        String appName = app.getName();
        String appLocation = app.getAbsolutePath();
        log(getMessage("info.deploy.app.configdropins", appLocation));

        File xmlApp = new File(overridesFolder, appName + ".xml");
        
        String xml = "<server>\n" + "<application name=\"" + appName + "\" location=\"" + appLocation + "\" ";
        
        if (contextRoot != null )
            xml += "context-root=\"" + contextRoot + "\"";
        if (commonLibraryRef != null)
            xml += ">\n<classloader commonLibraryRef=\"" + commonLibraryRef + "\" />" + "\n</application>\n";
        else 
            xml += "/>\n";
        
        xml += "</server>";
        
        try {
            xmlApp.createNewFile();
            FileWriter fileWriter = new FileWriter(xmlApp.getAbsoluteFile());
            BufferedWriter buffer = new BufferedWriter(fileWriter);
            buffer.write(xml);
            buffer.close();
        } catch (IOException e) {
            throw new BuildException(getMessage("error.deploy.fail", xmlApp.getPath()));
        }
        checkDeploy(app, xmlApp, true);
    }
	
    public void dropins(File srcFile, File destFile) {
        log(getMessage("info.deploy.app", srcFile.getPath()));
        try {
            FileUtils.getFileUtils().copyFile(srcFile, destFile, null, true);
        } catch (IOException e) {
            throw new BuildException(getMessage("error.deploy.fail"));
        }
        // Check start message code
        checkDeploy(srcFile, destFile, false);
    }
    
    private void checkDeploy(File srcFile, File destFile, boolean deleteIfFail) {
        // Check start message code
        String startMessage = START_APP_MESSAGE_CODE_REG + getFileName(destFile.getName());
        if (waitForStringInLog(startMessage, appStartTimeout, getLogFile()) == null) {
            if (deleteIfFail) {
                destFile.delete();
            }
            throw new BuildException(getMessage("error.deploy.fail", srcFile.getPath()));
        }
    }

}
