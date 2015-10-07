import java.io.Serializable;

public class Timer implements Serializable {
    private final MainView mainView;
    private Boolean isPaused = false;
    private Thread timerThread;

    public Timer(MainView mainView) {
        this.mainView = mainView;
    }

    void initTimerThread(final RunManager runManager) {
//        mainView.setTimerThread(new Thread() {
            timerThread = new Thread() {
            @Override
            public void run() {
                // this is what happens when timer.start() is implemented
                try {
                    mainView.getTimerThread().sleep(1000);
                    while (runManager.getIsRunning()) {
                        if(isPaused) {
                            pauseTimer();
                        }
                        int[] secondsArray = new int[]{mainView.getSeconds(), mainView.getTotalSeconds()};
                        int[] minutesArray = new int[]{mainView.getMinutes(), mainView.getTotalMinutes()};
                        int[] hoursArray = new int[]{mainView.getHours(), mainView.getTotalHours()};

                        for (int i = 0; i < secondsArray.length; i++) {
                            int thisSeconds = secondsArray[i];
                            int thisMinutes = minutesArray[i];
                            int thisHours = hoursArray[i];
                            if (thisSeconds == 59) {
                                if (thisMinutes == 59) {
                                    thisHours++;
                                    hoursArray[i] = thisHours;
                                    thisMinutes = 0;
                                    minutesArray[i] = thisMinutes;
                                    thisSeconds = 0;
                                    secondsArray[i] = thisSeconds;
                                } else {
                                    thisMinutes++;
                                    minutesArray[i] = thisMinutes;
                                    thisSeconds = 0;
                                    secondsArray[i] = thisSeconds;
                                }
                            } else {
                                thisSeconds++;
                                secondsArray[i] = thisSeconds;
                            }

                        }
                        mainView.setHours(hoursArray[0]);
                        mainView.setMinutes(minutesArray[0]);
                        mainView.setSeconds(secondsArray[0]);
                        mainView.setTotalHours(hoursArray[1]);
                        mainView.setTotalMinutes(minutesArray[1]);
                        mainView.setTotalSeconds(secondsArray[1]);

                        String[] strHoursArray = new String[]{String.valueOf(mainView.getHours()), String.valueOf(mainView.getTotalHours())};
                        String[] strMinutesArray = new String[]{String.valueOf(mainView.getMinutes()), String.valueOf(mainView.getTotalMinutes())};
                        String[] strSecondsArray = new String[]{String.valueOf(mainView.getSeconds()), String.valueOf(mainView.getTotalSeconds())};

                        for (int i = 0; i < strSecondsArray.length; i++) {
                            int thisHours = hoursArray[i];
                            int thisMinutes = minutesArray[i];
                            int thisSeconds = secondsArray[i];

                            String thisStrHours = strHoursArray[i];
                            String thisStrMinutes = strMinutesArray[i];
                            String thisStrSeconds = strSecondsArray[i];

                            if (thisHours < 10) {
                                strHoursArray[i] = "0" + thisHours;
                            } else {
                                strHoursArray[i] = String.valueOf(thisHours);
                            }

                            if (thisMinutes < 10) {
                                strMinutesArray[i] = "0" + thisMinutes;
                            } else {
                                strMinutesArray[i] = String.valueOf(thisMinutes);
                            }

                            if (thisSeconds < 10) {
                                strSecondsArray[i] = "0" + thisSeconds;
                            } else {
                                strSecondsArray[i] = String.valueOf(thisSeconds);
                            }

                            mainView.setStrHours(strHoursArray[0]);
                            mainView.setStrMinutes(strMinutesArray[0]);
                            mainView.setStrSeconds(strSecondsArray[0]);

                            mainView.setStrTotalHours(strHoursArray[1]);
                            mainView.setStrTotalMinutes(strMinutesArray[1]);
                            mainView.setStrTotalSeconds(strSecondsArray[1]);
                        }

                        mainView.getRunElapsedUI().setText(mainView.getStrHours() + ":" + mainView.getStrMinutes() + ":" + mainView.getStrSeconds());
                        mainView.getTimerThread().sleep(1000);
                    }

                } catch (InterruptedException e) {
                    mainView.getUtils().println("InterruptedException");
                    e.printStackTrace();
                }
            }
        };
    }

    private void pauseTimer() {
        while(isPaused) {
            try {
                Thread.currentThread().sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setIsPaused(Boolean isPaused) {
        this.isPaused = isPaused;
    }

    public void startTimer() {
        timerThread.start();
    }
}