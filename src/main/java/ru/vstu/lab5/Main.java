package ru.vstu.lab5;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import ru.vstu.lab5.agents.TicTacToeAgent;

/**
 * Точка входа для локального запуска лабораторной работы.
 * Создает главный контейнер JADE с GUI и запускает двух агентов,
 * которые играют друг против друга в крестики-нолики.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws StaleProxyException {
        Runtime runtime = Runtime.instance();

        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");
        profile.setParameter(Profile.MAIN, "true");

        ContainerController container = runtime.createMainContainer(profile);

        AgentController playerX = container.createNewAgent(
                "playerX",
                TicTacToeAgent.class.getName(),
                new Object[]{"starter"}
        );

        AgentController playerO = container.createNewAgent(
                "playerO",
                TicTacToeAgent.class.getName(),
                new Object[]{}
        );

        playerX.start();
        playerO.start();
    }
}
