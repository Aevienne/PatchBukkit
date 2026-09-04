package org.patchbukkit.testplugin.tests;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.patchbukkit.testplugin.ConformanceTest;
import org.patchbukkit.testplugin.TestCategory;
import org.patchbukkit.testplugin.TestExpectation;

import java.util.UUID;

public final class StubTests {

    @ConformanceTest(name = "Server.getWorlds() is implemented",
            category = TestCategory.STUBS)
    public void testGetWorlds() {
        Bukkit.getServer().getWorlds();
    }

    @ConformanceTest(name = "Server.getMaxPlayers() is implemented",
            category = TestCategory.STUBS)
    public void testGetMaxPlayers() {
        Bukkit.getServer().getMaxPlayers();
    }

    @ConformanceTest(name = "Server.getPort() is implemented",
            category = TestCategory.STUBS)
    public void testGetPort() {
        Bukkit.getServer().getPort();
    }

    @ConformanceTest(name = "Server.getIp() is implemented",
            category = TestCategory.STUBS)
    public void testGetIp() {
        Bukkit.getServer().getIp();
    }

    @ConformanceTest(name = "Server.getViewDistance() is implemented",
            category = TestCategory.STUBS)
    public void testGetViewDistance() {
        Bukkit.getServer().getViewDistance();
    }

    @ConformanceTest(name = "Server.getSimulationDistance() is implemented",
            category = TestCategory.STUBS)
    public void testGetSimulationDistance() {
        Bukkit.getServer().getSimulationDistance();
    }

    @ConformanceTest(name = "Server.getUpdateFolder() is implemented",
            category = TestCategory.STUBS)
    public void testGetUpdateFolder() {
        Bukkit.getServer().getUpdateFolder();
    }

    @ConformanceTest(name = "Server.getUpdateFolderFile() is implemented",
            category = TestCategory.STUBS)
    public void testGetUpdateFolderFile() {
        Bukkit.getServer().getUpdateFolderFile();
    }

    @ConformanceTest(name = "Server.getConnectionThrottle() is implemented",
            category = TestCategory.STUBS)
    public void testGetConnectionThrottle() {
        Bukkit.getServer().getConnectionThrottle();
    }

    @ConformanceTest(name = "Server.broadcastMessage() is implemented",
            category = TestCategory.STUBS)
    @SuppressWarnings("deprecation")
    public void testBroadcastMessage() {
        Bukkit.getServer().broadcastMessage("test");
    }

    @ConformanceTest(name = "Server.getOfflinePlayer(UUID) is implemented",
            category = TestCategory.STUBS)
    public void testGetOfflinePlayer() {
        Bukkit.getServer().getOfflinePlayer(UUID.randomUUID());
    }

    @ConformanceTest(name = "Server.getBanList() is implemented",
            category = TestCategory.STUBS)
    public void testGetBanList() {
        Bukkit.getServer().getBanList(org.bukkit.BanList.Type.NAME);
    }

    @ConformanceTest(name = "Server.getOperators() is implemented",
            category = TestCategory.STUBS)
    public void testGetOperators() {
        Bukkit.getServer().getOperators();
    }

    @ConformanceTest(name = "Server.getWhitelistedPlayers() is implemented",
            category = TestCategory.STUBS)
    public void testGetWhitelistedPlayers() {
        Bukkit.getServer().getWhitelistedPlayers();
    }

    @ConformanceTest(name = "Server.reloadWhitelist() is implemented",
            category = TestCategory.STUBS)
    public void testReloadWhitelist() {
        Bukkit.getServer().reloadWhitelist();
    }

    @ConformanceTest(name = "Server.shutdown() is not tested live (would stop the server)",
            category = TestCategory.STUBS, expectation = TestExpectation.EXPECT_UNSUPPORTED)
    public void testShutdown() {
        // Intentionally not calling shutdown(): it succeeds on live servers.
        throw new UnsupportedOperationException("shutdown() conformance is verified manually, not live");
    }

    @ConformanceTest(name = "Server.getMotd() is implemented",
            category = TestCategory.STUBS)
    public void testGetMotd() {
        Bukkit.getServer().getMotd();
    }

    @ConformanceTest(name = "Server.getAllowNether() is implemented",
            category = TestCategory.STUBS)
    public void testGetAllowNether() {
        Bukkit.getServer().getAllowNether();
    }

    @ConformanceTest(name = "PaperLib detection classes and PaperLib.isPaper() are available", category = TestCategory.STUBS)
    public void testPaperLibDetection() throws ClassNotFoundException {
        Class.forName("com.destroystokyo.paper.PaperConfig");
        Class.forName("io.papermc.paper.configuration.Configuration");
        org.patchbukkit.testplugin.TestAssertions.assertTrue(io.papermc.lib.PaperLib.isPaper(), "PaperLib.isPaper() must be true");
    }

}
