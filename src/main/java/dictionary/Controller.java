package dictionary;

import dictionary.services.exporters.FullDictionaryExporter;
import dictionary.services.exporters.SelectedWordsExporter;
import dictionary.services.improvers.TransitivityImprover;
import dictionary.services.mergers.DictionaryMerger;
import dictionary.services.statistics.TransitivityStatisticsCollector;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller {

    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.start();
    }

    public void start() {
        Map<String, Object> config = getConfigData();
        String task = (String) config.get("task");

        if (task.equals("load_contents")) {
            List<Runnable> loaders = new ArrayList<>();
            List<String> contexts = (ArrayList<String>) config.get("contexts_for_dictionaries");
            for (String context : contexts) {
                ApplicationContext ctx = new ClassPathXmlApplicationContext("spring_contexts/" + context);
                loaders.add((Runnable) ctx.getBean("contentsLoader"));
            }
            runLoaders(loaders);

        } else if (task.equals("load_synonyms")) {
            List<Runnable> loaders = new ArrayList<>();
            List<String> contexts = (ArrayList<String>) config.get("contexts_for_dictionaries");
            for (String context : contexts) {
                ApplicationContext ctx = new ClassPathXmlApplicationContext("spring_contexts/" + context);
                loaders.add((Runnable) ctx.getBean("synonymLoader"));
            }
            runLoaders(loaders);

        } else if (task.equals("merge_dictionaries")) {
            String context = (String) config.get("merge_context");
            ApplicationContext ctx = new ClassPathXmlApplicationContext("spring_contexts/" + context);
            DictionaryMerger dictionaryMerger = (DictionaryMerger) ctx.getBean("dictionaryMerger");
            dictionaryMerger.mergeDictionaries();

        } else if (task.equals("improve_dictionary")) {
            String context = (String) config.get("improvement_context");
            ApplicationContext ctx = new ClassPathXmlApplicationContext("spring_contexts/" + context);
            TransitivityImprover transitivityImprover = (TransitivityImprover) ctx.getBean("transitivityImprover");
            transitivityImprover.improveDictionary();

        } else if (task.equals("export_dictionary")) {
            String context = (String) config.get("export_context");
            // List<String> words = (List<String>) config.get("words");
            ApplicationContext ctx = new ClassPathXmlApplicationContext("spring_contexts/" + context);
            // SelectedWordsExporter selectedWordsExporter = (SelectedWordsExporter) ctx.getBean("selectedWordsExporter");
            // selectedWordsExporter.exportSelectedWords(words);
            FullDictionaryExporter fullDictionaryExporter = (FullDictionaryExporter) ctx.getBean("fullDictionaryExporter");
            fullDictionaryExporter.exportFullDictionary();

        } else if (task.equals("gather_statistics")) {
            String context = (String) config.get("statistics_context");
            ApplicationContext ctx = new ClassPathXmlApplicationContext("spring_contexts/" + context);
            TransitivityStatisticsCollector transitivityStatisticsCollector =
                    (TransitivityStatisticsCollector) ctx.getBean("transitivityStatisticsCollector");
            System.out.println(transitivityStatisticsCollector.getProbabilityThatTransitivityWorks());
        }
    }

    private void runLoaders(List<Runnable> loaders) {
        ExecutorService executor = Executors.newFixedThreadPool(loaders.size());
        for (Runnable loader : loaders) {
            executor.execute(loader);
        }
        executor.shutdown();
        waitUntilStopCommandOrTermination(executor);
    }

    private void waitUntilStopCommandOrTermination(ExecutorService executor) {
        BufferedReader reader = new BufferedReader( new InputStreamReader(System.in));
        while (!executor.isTerminated()) {
            try {
                String userCommand = reader.readLine();
                if (userCommand.equals("stop")) {
                    // Shutting down the program
                    executor.shutdownNow();
                    break;
                }

            } catch (IOException e) {
                // Ignore
            }
        }
        System.out.println("Console listener was terminated");
    }

    private Map<String, Object> getConfigData() {
        ObjectMapper mapper = new ObjectMapper();
        String configPath = Controller.class.getResource("/config.json").getPath();
        Map<String, Object> config = null;
        try {
            config = mapper.readValue(new File(configPath), HashMap.class);

        } catch (Exception e) {
            System.err.println("Unable to read configuration file");
            e.printStackTrace();
            System.exit(1);
        }
        return config;
    }
}
