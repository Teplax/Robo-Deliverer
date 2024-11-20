import java.util.*;

public class FrequencyLogger {
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        final int THREAD_QUANTITY = 1000; //задаём константу количества потоков
        List<Thread> threads = new ArrayList<>(); //создаём список потоков
        for (int i = 0; i < THREAD_QUANTITY; i++) {
            threads.add( //добавляем потоки в список
                    new Thread(() -> {//логика потока
                        String result = generateRoute("RLRFR", 100);//генерируем строку маршрута
                        int rs = countRs(result, 'R');//считаем количество поворотов направо в маршруте
                        synchronized (sizeToFreq) {//определяем критическую секцию
                            if (sizeToFreq.containsKey(rs)) {//наполняем мапу - если такая частота уже есть
                                sizeToFreq.replace(rs, sizeToFreq.get(rs) + 1);//добавляем 1 к количеству её повторений
                            } else {
                                sizeToFreq.put(rs, 1); //если такой частоты нет -добавляем её в мапу со значением повторений 1
                            }
                            sizeToFreq.notifyAll();
                        }
                    }

                    )
            );
        }
        Runnable logTask = () -> {
            int i = 1;
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int currentMaxKey = Collections.max(sizeToFreq.entrySet(), Map.Entry.comparingByValue()).getKey();
                    System.out.printf("%d. Текущий максимум: %d, повторился %d раз%n", i++, currentMaxKey,
                            sizeToFreq.get(currentMaxKey));
                }
            }
        };
        Thread loggerThread = new Thread(logTask);
        loggerThread.start();
        for (Thread thread : threads) {//запускаем потоки из списка на выполнение
            thread.start();
        }

        for (Thread thread : threads) {//ожидание завершения выполнения всех запущенных потоков основным потоком программы
            thread.join();
        }
        loggerThread.interrupt();
        int maxKey = Collections.max(sizeToFreq.entrySet(), Map.Entry.comparingByValue()).getKey();//находим частоту с максимальным
        // количеством повторений и выводим её на экран:
        System.out.printf("Самое частое количество повторений: %d (встретилось %d раз)\n", maxKey,
                sizeToFreq.get(maxKey));
        sizeToFreq.remove(maxKey);//удаляем её из мапы
        sizeToFreq.forEach((a, b) -> System.out.printf("%d (%d раз)\n", a, b));//выводим оставшиеся частоты на экран

    }

    public static int countRs(String route, char turn) { //функция подсчёта поворотов
        int countR = 0;
        for (char r : route.toCharArray()) {
            if (r == turn) countR++;
        }
        return countR;
    }

    public static String generateRoute(String letters, int length) { //функция генерации случайного маршрута
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}
