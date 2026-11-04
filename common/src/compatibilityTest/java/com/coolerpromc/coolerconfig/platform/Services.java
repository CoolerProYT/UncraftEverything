package com.coolerpromc.coolerconfig.platform;

public class Services {
    public static final com.coolerpromc.coolerconfig.platform.services.IPlatformHelper PLATFORM = new com.coolerpromc.coolerconfig.platform.services.IPlatformHelper(){
        public String getPlatformName(){
            return "Regression";
        }
        public boolean isModLoaded(String id){
            return false;
        }
        public boolean isDevelopmentEnvironment(){
            return true;
        }
        public boolean isPhysicalClient(){
            return true;
        }
        public java.nio.file.Path getConfigDirectory(){
            return java.nio.file.Path.of("config");
        }
    };
}
