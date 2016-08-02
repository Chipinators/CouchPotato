package paperprisoners.couchpotato;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class WouldChuckFragment extends Fragment implements MessageListener {
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //GLOBAL VARS
    private static final String TAG = "WC_Fragment";
    int stage;
    private boolean host, cont = false;
    private UserData me; //Will be passed to GameActivity, pull from there later
    private int gameRound;
    private String[][] responses; //responses[playerid][submission] -- responses[players.size() - 1][1]
    private String[] submissions;
    private boolean[] input = new boolean[2];
    private int responsesLeft;
    private int vtePlayer1, vtePlayer2;//store the owners of votes1 and votes2
    private int votes1, votes2;
    private ArrayList<UserData> players;
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private boolean submissionsArePaired;
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private Thread start;

    @Override
    public synchronized void onStart() {
        super.onStart();
        loading("Setting up the game...");

        BluetoothService.listeners.add(this);

        players = ((GameActivity) getActivity()).getPlayers(); //store the players
        me = ((GameActivity) getActivity()).getMe();
        host = ((GameActivity) getActivity()).getHost();

        if(Constants.debug){Log.i(TAG, "PLAYERS:" + players.size());}

        for (int i = 0; i < players.size(); i++) {
            if(Constants.debug){Log.i(TAG, players.get(i).username + " - " + me.username + ": " + players.get(i).username.equals(me.username));}
            if (players.get(i).username.equals(me.username)) {
                if(Constants.debug){Log.i(TAG, "I AM HERE");}
                me.setPlayerID(players.get(i).playerID);
                break;
            }
        }

        if(Constants.debug){Log.i(TAG, "ME ID: " + me.getPlayerID());}

        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //region Start Thread
        start = new Thread() {
            @Override
            public void run() {
                if(Constants.debug){Log.i(TAG, "IN THREAD");}
                try {
                    stage = 0;
                    gameRound = 1; //initalize the starting round
                    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
                    while (gameRound <= 3) { //loop through the rounds!
                        showSlashScreen();
                        submissionsArePaired = false;
                        resultsRecieved = 0;
                        responsesLeft = players.size() * 2; //number of responses
                        stage = 1;

                        usersEnterValues();//get the user's submissions

                        while (true) {
                            if (stage % 2 == 0) {
                                break;
                            }
                        }
                        while (responsesLeft > 0) { //loop through the submissions to let user's vote

                            playerVoting(); //playerID's cast vote
                            while (true) {
                                if (stage == 3) {
                                    break;
                                }
                            }
                            showVotingResults(); //results of vote are shown
                            votes1 = 0;
                            votes2 = 0;
                            vtePlayer1 = -1;
                            vtePlayer2 = -1;
                            submissions = null;
                            responsesLeft = responsesLeft - 2; //update responses to pars
                        }
                        showRoundResults();//show the final winner screen
                        stage = 4;
                        gameRound++;
                    }
                    gameOver();//call the game over screen!
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage()); //ERROR (in case you fuck heads didn't know)
                }
            }
        };
        if(Constants.debug){Log.i(TAG, "THREAD START");}
        start.start(); //Run the game thread!
        if(Constants.debug){Log.i(TAG, "THREAD END");}
        //endregion
    } //DONE

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //region Game Code
    public void showSlashScreen() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setContentView(R.layout.wouldchuck_round);
                int dRID = getActivity().getResources().getIdentifier("@drawable/round" + gameRound + "_512", "drawable", "paperprisoners.couchpotato");
                ImageView rv = (ImageView) getActivity().findViewById(R.id.wc_round_img);
                if(Constants.debug){Log.i(TAG, "DRID: " + dRID);}
                rv.setImageResource(dRID);
            }
        });

        Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                cont = true;
            }
        }, 2500);

        while (true) {
            if (cont) {
                if(Constants.debug){Log.i(TAG, "PAST DELAY");}
                cont = false;
                break;
            }
        }
    }//done

    private boolean buttonPressed = false;
    private String input1;
    private String input2;

    public void usersEnterValues() {

        final int time =45 ;
        buttonPressed = false;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(Constants.debug){Log.i(TAG, "HERE!");}
                getActivity().setContentView(R.layout.wouldchuck_input);
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler timer = new Handler();
                Runnable clock = new Runnable() {
                    TextView timerText = (TextView) getActivity().findViewById(R.id.wc_input_timer);
                    int t = time;

                    @Override
                    public void run() {
                        if (t > 0) {
                            timerText.setText("" + t);
                            timerText.invalidate();
                            t--;
                            timer.postDelayed(this, 1000);
                        } else {

                        }
                    }
                };
                timer.post(clock);

                Button submit = (Button) getActivity().findViewById(R.id.wc_input_submit);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        input1 = ((TextView) getActivity().findViewById(R.id.wc_input_1)).getText().toString();//store rather 1
                        input2 = ((TextView) getActivity().findViewById(R.id.wc_input_2)).getText().toString();//store rather 2

                        buttonPressed = true;

                        loading("Please wait...");
                    }
                });
            }
        });

        Handler handler2 = new Handler(Looper.getMainLooper());
        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                if (!buttonPressed) {
                    if(Constants.debug){Log.i(TAG, "Start Second Runnable (Pre data from users)");}
                    input1 = ((TextView) getActivity().findViewById(R.id.wc_input_1)).getText().toString();//store rather 1
                    input2 = ((TextView) getActivity().findViewById(R.id.wc_input_2)).getText().toString();//store rather 2
                    ((TextView) getActivity().findViewById(R.id.wc_input_1)).setText("");//clear
                    ((TextView) getActivity().findViewById(R.id.wc_input_2)).setText("");//clear
                    getActivity().findViewById(R.id.wc_input_1).invalidate();//clear
                    getActivity().findViewById(R.id.wc_input_2).invalidate();//clear
                    //when submit hit or time run out
                }
                getDataFromUsers(input1, input2);//parse and store the data

                if(Constants.debug){Log.i(TAG, "End Second Runnable (Post data from users)");}
            }
        };
        handler2.postDelayed(r2, time * 1000);

    } //DONE

    public void getDataFromUsers(String input1, String input2) {
        if(Constants.debug){Log.i(TAG, "getDataFromUsers called");}
        responses = new String[players.size()][2]; //here so if players drop out we dont expect stuff from them. also clears array
        Arrays.fill(responses, new String[]{"", ""});

        if (input1.equals("")) {
            input1 = "User Didn't Enter a Value";
        }
        if (input2.equals("")) {
            input2 = "User Didn't Enter a Value";
        }

        if (host) {
            if(Constants.debug){Log.i(TAG, "Set host's responses");}
            responses[0] = new String[]{input1, input2};

            if(Constants.debug){Log.i(TAG, "host's responses written");}
            stage = 2;
            if(Constants.debug){Log.i(TAG, "In host - HOST");}
            cont = false;

        } else {
            if(Constants.debug){Log.i(TAG, "Not in host - CLIENT");}
            BluetoothService.writeToServer("" + me.playerID, Constants.WC_SUBMISSION, new String[]{input1, input2});
            if(Constants.debug){Log.i(TAG, me.playerID + ": WROTE TO HOST: " + input1 + ", " + input2);}
            stage = 2;
            cont = false;
        }
    } //DONE

    public int numberOfVotesIn;

    public void playerVoting() {
        vtePlayer2 = -1;
        vtePlayer1 = -1;
        input = null;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setContentView(R.layout.wouldchuck_choice);
            }
        });

        if (host) {
            if(Constants.debug){Log.i(TAG, "LENGTH HOST: " + responses[0][0]);}
            if(Constants.debug){Log.i(TAG, "LENGTH CLIENT: " + responses[1][0]);}

            submissions = selectRathers(); //generate submissions
            if(Constants.debug){Log.i(TAG, "RATHERS: " + submissions[0] + " | " + submissions[1] + " | " + submissions[2] + " | " + submissions[3]);}

            BluetoothService.writeToClients(Constants.WC_QUESTION, submissions);//send the data over to clients

            vtePlayer1 = Integer.parseInt(submissions[2]);//store the owner ints //HOSTS ONLY
            vtePlayer2 = Integer.parseInt(submissions[3]);//store the owner ints
            submissions = new String[]{submissions[0], submissions[1]};//remove the ints
        } else {
            while (vtePlayer1 == -1 || vtePlayer2 == -1) {
            }
        }
        if(Constants.debug){Log.i(TAG, "PAST THE WAITING");}
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setContentView(R.layout.wouldchuck_choice);
                Button ch1 = (Button) getActivity().findViewById(R.id.wc_choice_1);
                Button ch2 = (Button) getActivity().findViewById(R.id.wc_choice_2);
                ch1.setText(submissions[0]); //set option 1
                ch2.setText(submissions[1]); //set option 2
                ch1.invalidate();
                ch2.invalidate();
            }
        });
        if(Constants.debug){Log.i(TAG, "PAST THE UI THREAD");}
        input = new boolean[2];
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button ch1 = (Button) getActivity().findViewById(R.id.wc_choice_1);
                Button ch2 = (Button) getActivity().findViewById(R.id.wc_choice_2);

                if (!ch1.getText().equals("User Didn't Enter a Value") && !ch2.getText().equals("User Didn't Enter a Value")) {
                    ch1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Button ch1 = (Button) getActivity().findViewById(R.id.wc_choice_1);
                            Button ch2 = (Button) getActivity().findViewById(R.id.wc_choice_2);

                            if(Constants.debug){Log.i(TAG, "VOTED FOR 1st Element");}
                            input[0] = true;
                            input[1] = false;
                            ch2.setEnabled(true);
                            ch1.setEnabled(false);

                            ch1.setAlpha((float) .25);
                            ch2.setAlpha((float) 1);
                        }
                    });
                    ch2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Button ch1 = (Button) getActivity().findViewById(R.id.wc_choice_1);
                            Button ch2 = (Button) getActivity().findViewById(R.id.wc_choice_2);

                            if(Constants.debug){Log.i(TAG, "VOTED FOR 2nd Element");}
                            input[1] = true;
                            input[0] = false;
                            ch2.setEnabled(false);
                            ch1.setEnabled(true);

                            ch2.setAlpha((float) .25);
                            ch1.setAlpha((float) 1);
                        }
                    });
                } else {
                    ch1.setEnabled(false);
                    ch1.setAlpha((float) 1);
                    ch1.setTextColor(getActivity().getResources().getColor(R.color.main_deny));
                    ch2.setAlpha((float) 1);
                    ch2.setTextColor(getActivity().getResources().getColor(R.color.main_deny));
                }
            }
        });

        cont = false;
        if(Constants.debug){Log.i(TAG, "DELAY");}
        Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                cont = true;
            }
        }, 15000);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler timer = new Handler();
                Runnable clock = new Runnable() {
                    TextView timerText = (TextView) getActivity().findViewById(R.id.wc_choice_timer);
                    Button ch1 = (Button) getActivity().findViewById(R.id.wc_choice_1);
                    Button ch2 = (Button) getActivity().findViewById(R.id.wc_choice_2);
                    int time = 15;

                    @Override
                    public void run() {
                        if (time > 0) {
                            timerText.setText("" + time);
                            timerText.invalidate();
                            time--;
                            timer.postDelayed(this, 1000);
                        } else {

                        }
                    }
                };
                timer.post(clock);
            }
        });

        while (true) {
            if (cont) {
                if(Constants.debug){Log.i(TAG, "PAST DELAY");}
                cont = false;
                break;
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setContentView(R.layout.wouldchuck_waiting);
            }
        });
        numberOfVotesIn = 0;
        sendUserVotes(input);
        stage = 3;
    } //DONE




    public void showVotingResults() {
        if(Constants.debug){Log.i(TAG, "Show Results");}
        loading("Gathering Votes...");
        cont = false;

        if (host) {
            while (true) { //while waiting to recieve votes
                if (numberOfVotesIn == players.size() - 1) {
                    break;
                }
            }
            BluetoothService.writeToClients(Constants.WC_RESULTS, new String[]{"" + vtePlayer1, "" + vtePlayer2, "" + votes1, "" + votes2});
            if(Constants.debug){Log.i(TAG, "Sent Results to Clients: id" + vtePlayer1 + " - num votes " + votes1 + ", id" + vtePlayer2 + " - num votes " + votes2);}
            cont = true;
        } else {

        }
        while (true) {//while waiting to recieve vote results
            if (cont) {
                cont = false;
                break;
            }
        }

        Handler wait = new Handler(Looper.getMainLooper());
        wait.postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(Constants.debug){Log.i(TAG, "Viewing Results");}
                        getActivity().setContentView(R.layout.wouldchuck_results);
                        ((TextView) getActivity().findViewById(R.id.wc_results_content1)).setText(submissions[0]);
                        getActivity().findViewById(R.id.wc_results_content1).invalidate();
                        ((TextView) getActivity().findViewById(R.id.wc_results_content2)).setText(submissions[1]);
                        getActivity().findViewById(R.id.wc_results_content2).invalidate();
                        ((TextView) getActivity().findViewById(R.id.wc_results_count1)).setText(votes1 + "");
                        getActivity().findViewById(R.id.wc_results_count1).invalidate();
                        ((TextView) getActivity().findViewById(R.id.wc_results_count2)).setText(votes2 + "");
                        getActivity().findViewById(R.id.wc_results_count2).invalidate();
                        ((TextView) getActivity().findViewById(R.id.wc_results_user1)).setText(players.get(findPlayerIndex(vtePlayer1)).username);
                        getActivity().findViewById(R.id.wc_results_user1).invalidate();
                        ((TextView) getActivity().findViewById(R.id.wc_results_user2)).setText(players.get(findPlayerIndex(vtePlayer2)).username);
                        getActivity().findViewById(R.id.wc_results_user2).invalidate();

                        if (submissions[0].equals("User Didn't Enter a Value") && submissions[1].equals("User Didn't Enter a Value")) {
                            ((TextView) getActivity().findViewById(R.id.wc_results_score1)).setText(-1 * ((500 * gameRound) / 2) + "");
                            getActivity().findViewById(R.id.wc_results_score1).invalidate();
                            ((TextView) getActivity().findViewById(R.id.wc_results_score2)).setText(-1 * ((500 * gameRound) / 2) + "");
                            getActivity().findViewById(R.id.wc_results_score2).invalidate();
                        } else if (submissions[0].equals("User Didn't Enter a Value")) {
                            ((TextView) getActivity().findViewById(R.id.wc_results_score2)).setText(((500 * gameRound) / 2) + "");
                            getActivity().findViewById(R.id.wc_results_score1).invalidate();
                            ((TextView) getActivity().findViewById(R.id.wc_results_score1)).setText(-1 * ((500 * gameRound) / 2) + "");
                            getActivity().findViewById(R.id.wc_results_score2).invalidate();
                        } else if (submissions[1].equals("User Didn't Enter a Value")) {
                            ((TextView) getActivity().findViewById(R.id.wc_results_score1)).setText((500 * gameRound) / 2 + "");
                            getActivity().findViewById(R.id.wc_results_score1).invalidate();
                            ((TextView) getActivity().findViewById(R.id.wc_results_score2)).setText((-1 * (500 * gameRound) / 2) + "");
                            getActivity().findViewById(R.id.wc_results_score2).invalidate();
                        } else if (votes1 < votes2) {//number 1 wins
                            ((TextView) getActivity().findViewById(R.id.wc_results_score1)).setText((500 * gameRound) + "");
                            getActivity().findViewById(R.id.wc_results_score1).invalidate();
                            ((TextView) getActivity().findViewById(R.id.wc_results_score2)).setText("0");
                            getActivity().findViewById(R.id.wc_results_score2).invalidate();
                        } else if (votes2 < votes1) {//number 2 wins
                            ((TextView) getActivity().findViewById(R.id.wc_results_score1)).setText("0");
                            getActivity().findViewById(R.id.wc_results_score1).invalidate();
                            ((TextView) getActivity().findViewById(R.id.wc_results_score2)).setText((500 * gameRound) + "");
                            getActivity().findViewById(R.id.wc_results_score2).invalidate();
                        } else {//tie
                            ((TextView) getActivity().findViewById(R.id.wc_results_score1)).setText((250 * gameRound) + "");
                            getActivity().findViewById(R.id.wc_results_score1).invalidate();
                            ((TextView) getActivity().findViewById(R.id.wc_results_score2)).setText((250 * gameRound) + "");
                            getActivity().findViewById(R.id.wc_results_score2).invalidate();
                        }
                    }
                });
            }
        }, 1000);

        cont = false;
        Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                cont = true;
            }
        }, 7500);
        if(Constants.debug){Log.i(TAG, "Delay");}
        while (true) {
            if (cont) {
                cont = false;
                break;
            }
        }
        if(Constants.debug){Log.i(TAG, "Done Delay");}
        savePlayerPoints();
    }//TODO: FIX?

    public void showRoundResults() {
        if(Constants.debug){Log.i(TAG, "Round Results");}

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(Constants.debug){Log.i(TAG, "Set order on leader board");}
                int[][] order = playerOrder();
                getActivity().setContentView(R.layout.wouldchuck_leaderboard);
                for (int i = 0; i < order.length; i++) {
                    if(Constants.debug){Log.i(TAG, "ORDER: ID - " + order[i][0] + ", POINTS - " + order[i][1]);}
                    ((TextView) getActivity().findViewById(getResources().getIdentifier("wc_leaderboard_name" + (i + 1), "id", "paperprisoners.couchpotato"))).setText(players.get(order[i][0]).username); //SEARCH STRING TO RESOURCE ID
                    ((TextView) getActivity().findViewById(getResources().getIdentifier("wc_leaderboard_score" + (i + 1), "id", "paperprisoners.couchpotato"))).setText("" + order[i][1]);
                }

                LinearLayout parent = (LinearLayout) getActivity().findViewById(R.id.wc_leaderboard_parentLayout);
                for (int i = order.length; i < 8; i++) {
                    parent.removeView(getActivity().findViewById(getResources().getIdentifier("wc_leaderboard_layout" + (i + 1), "id", "paperprisoners.couchpotato")));
                }
            }
        });

        cont = false;
        Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                cont = true;
            }
        }, 7500);
        if(Constants.debug){Log.i(TAG, "Delay");}
        while (true) {
            if (cont) {
                cont = false;
                break;
            }
        }
        if(Constants.debug){Log.i(TAG, "Done Delay");}
    } //DONE

    public int[][] playerOrder() {
        if(Constants.debug){Log.i(TAG, "Getting Player Order");}
        int[][] order = new int[responses.length][2];

        for (int i = 0; i < players.size(); i++) {
            order[i][0] = players.get(i).playerID;
            order[i][1] = players.get(i).score;
        }

        int tempID, tempPoints;
        for (int i = 1; i < order.length; i++) { //sort by points (highest to lowest)
            for (int j = i; j > 0; j--) {
                if (order[j][1] > order[j - 1][1]) {
                    tempID = order[j][0];
                    tempPoints = order[j][1];

                    order[j][0] = order[j - 1][0];
                    order[j][1] = order[j - 1][1];

                    order[j - 1][0] = tempID;
                    order[j - 1][1] = tempPoints;
                }
            }
        }
        return order;
    }//DONE

    public void gameOver() {
        if(Constants.debug){Log.i(TAG, "GAME OVER");}
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setContentView(R.layout.activity_select);
            }
        });

    } //TODO: Implement play again function

    //endregion
    //*********************************************************************************
    //*********************************************************************************
    @Override
    public void onReceiveMessage(int player, int messageType, Object[] content) {
        if(Constants.debug){Log.i(TAG, "RECIEVED MESSAGE " + messageType);}
        switch (messageType) {
            case Constants.NEXT:
                cont = true;
                break;
            case Constants.WC_SUBMISSION: //DONE
                if(Constants.debug){Log.i(TAG, "SUBMISSION");}
                int index = findPlayerIndex(player);
                responses[index] = new String[]{(String) content[0], (String) content[1]};
                resultsRecieved ++;
                break;
            case Constants.WC_QUESTION: //DONE
                vtePlayer1 = Integer.parseInt((String) content[2]);//store the owner ints
                vtePlayer2 = Integer.parseInt((String) content[3]);//store the owner ints
                submissions = new String[]{(String) content[0], (String) content[1]};//remove the ints
                if(Constants.debug){Log.i(TAG, "IMPORTED: " + vtePlayer1 + " | " + vtePlayer2 + " | " + submissions[0] + " | " + submissions[1]);}
                break;
            case Constants.WC_VOTE: //TODO: FIX?
                if (content[0].toString().toLowerCase().equals("false") && content[1].toString().toLowerCase().equals("false")) {
                    //no vote, no input
                } else if (content[0].toString().toLowerCase().equals("true")) {
                    votes1 = votes1 + 1;
                } else {
                    votes2 = votes2 + 1;
                }

                numberOfVotesIn++;
                if(Constants.debug){Log.i(TAG, "VOTE COUNT: v1 - " + votes1 + ", v2 - " + votes2);}
                break;
            case Constants.WC_RESULTS://TODO: FIX?
                vtePlayer1 = Integer.parseInt((String) content[0]);//store the owner ints
                vtePlayer2 = Integer.parseInt((String) content[1]);//store the owner ints
                votes1 = Integer.parseInt((String) content[2]);//store the owner ints
                votes2 = Integer.parseInt((String) content[3]);//store the owner ints
                cont = true;
                if(Constants.debug){Log.i(TAG, "Results: player" + vtePlayer1 + " - " + votes1 + ", player" + vtePlayer2 + " - " + votes2);}
                break;

        }
    } //TODO: FIX?
    //*********************************************************************************
    //*********************************************************************************

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    public int findPlayerIndex(int id) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).playerID == id) {
                return i;
            }
        }
        return -1;
    } //DONE

    public void loading(final String Message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setContentView(R.layout.wouldchuck_waiting);
                ((TextView) getActivity().findViewById(R.id.wc_waiting_text)).setText(Message);
            }
        });
    } //DONE

    private ArrayList<String[]> pairs;

    private int resultsRecieved = 0;
    public String[] selectRathers() {
        if(resultsRecieved != -1){
            while(true){
                if(resultsRecieved == players.size() - 1){
                    resultsRecieved = -1;
                    break;
                }
            }
        }

        //private boolean submissionsArePaired;
        int offset;
        int interval;
        int loopIndex;

        if (!submissionsArePaired) {
            Random rand = new Random();
            offset = rand.nextInt(players.size());
            interval = rand.nextInt(players.size() - 2) + 1;

            if (players.size() % 2 == interval % 2) {
                interval++;
            }

            pairs = new ArrayList<>();
            submissionsArePaired = true;

            int randPlayer;
            int counter = 0;

            String randQ;

            ArrayList<String[]> temp = new ArrayList<>();

            for (int i = 0; i < players.size() * 2; i++) {
                counter += offset;
                randPlayer = (counter) % players.size();

                randQ = responses[randPlayer][i%2];

                temp.add(new String[]{randQ, randPlayer + ""});
                if(Constants.debug){Log.i(TAG, "NEW PAIR: " + Arrays.toString(temp.get(temp.size() - 1)));}
            }

            for (int i = 0; i < temp.size(); i = i + 2) {
                pairs.add(new String[]{temp.get(i)[0], temp.get(i + 1)[0], temp.get(i)[1], temp.get(i + 1)[1]});
            }
        }

        String[] temp = pairs.get(0);
        Log.e(TAG, "PAIRs: " + Arrays.toString(temp));
        pairs.remove(0);
        return temp;
    } //DONE

    public void sendUserVotes(boolean[] vteResults) {
        if(Constants.debug){Log.i(TAG, "Get User's Votes");}
        if (host) {
            if (vteResults[0] == false && vteResults[1] == false) {
                //do nothing
            } else if (vteResults[0]) {
                votes1 = votes1 + 1;
            } else {
                votes2 = votes2 + 1;
            }
        } else {
            BluetoothService.writeToServer("" + me.playerID, Constants.WC_VOTE, new String[]{Boolean.toString(vteResults[0]), Boolean.toString(vteResults[1])});
            if(Constants.debug){Log.i(TAG, "Sent Votes to Host");}
        }
    } //TODO: FIX?

    public void savePlayerPoints() {
        if(Constants.debug){Log.i(TAG, "Saving points");}

        int points = gameRound * 500;
        int winner;

        if (submissions[0].equals("User Didn't Enter a Value") && submissions[1].equals("User Didn't Enter a Value")) {
            points = -1 * points / 2;
            winner = -1;
        } else if (submissions[0].equals("User Didn't Enter a Value")) {
            points = -1 * points / 2;
            winner = vtePlayer2;
        } else if (submissions[1].equals("User Didn't Enter a Value")) {
            points = -1 * points / 2;
            winner = vtePlayer1;
        } else if (votes1 < votes2) {
            winner = vtePlayer1;
        } else if (votes1 > votes2) {
            winner = vtePlayer2;
        } else {
            winner = -1;
            points = points / 2;
        }

        if (winner == -1) {
            players.get(vtePlayer1).score += points;
            players.get(vtePlayer2).score += points;
        } else {
            players.get(winner).score += points;
        }
    } //DONE

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //region Default Fragment Methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothService.addMessageListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_would_chuck, container, false);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    //endregion (N
}
