package eu.mikroskeem.orion.core.launcher.modlauncher;

import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;

import java.util.concurrent.Callable;

public final class OrionLaunchHandlerService implements ILaunchHandlerService {
    @Override
    public String name() {
        return "orion";
    }

    @Override
    public void configureTransformationClassLoader(ITransformingClassLoaderBuilder builder) {

    }

    @Override
    public Callable<Void> launchService(String[] arguments, ITransformingClassLoader launchClassLoader) {
        return null;
    }
}
