package paperprisoners.couchpotato;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class WouldChuckFragment extends Fragment implements MessageListener {
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //GLOBAL VARS
    private static final String TAG = "WC_Fragment";
    Handler handler = new Handler();
    int stage;
    private boolean host, cont = false;
    private UserData me; //Will be passed to GameActivity, pull from there later
    private int gameRound;
    private TextView clock;
    private String[][] responses;
    private String[] rathers;
    private int responsesLeft;
    private int vtePlayer1, vtePlayer2;//store the owners of votes1 and votes2
    private int votes1, votes2;
    private LayoutInflater inflater;
    private ArrayList<UserData> players;
    private ArrayList<Boolean> recieved;
    private SetupAdapter adapter;
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private Thread start;

    @Override
    public synchronized void onStart() {
        super.onStart();
        loading();

        players = ((GameActivity) getActivity()).getPlayers(); //store the players
        me = ((GameActivity) getActivity()).getMe();
        host = ((GameActivity) getActivity()).getHost();

        //region Start Thread
        start = new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "IN THREAD");
                try {
                    stage = 0;
                    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
                    gameRound = 1; //initalize the starting round
                    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().setContentView(R.layout.wouldchuck_round);
                            int dRID = getActivity().getResources().getIdentifier("@drawable/round" + gameRound + "_512", "drawable", "paperprisoners.couchpotato");
                            ImageView rv = (ImageView) getActivity().findViewById(R.id.wc_round_img);
                            Log.i(TAG, "DRID: " + dRID);
                            rv.setImageResource(dRID);
                        }
                    });

                    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
                    while (gameRound <= 3) { //loop through the rounds!

                        responsesLeft = players.size() * 2; //number of responses
                        stage = 1;
                        usersEnterValues();//get the user's rathers
                        while (true) {
                            if (stage % 2 == 0) {
                                break;
                            }
                        }
                        while (responsesLeft > 0) { //loop through the rathers to let user's vote
                            playerVoting(); //playerID's cast votes
                            showVotingResults(); //results of vote are shown
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
        Log.i(TAG, "THREAD START");
        start.start(); //Run the game thread!
        Log.i(TAG, "THREAD END");
        //endregion
    }

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //region Game Code
    public void usersEnterValues() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Handler handler1 = new Handler();
                Runnable r = new Runnable() {
                    public void run() {
                        Handler handler2 = new Handler();
                        Runnable r2 = new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "I MADE IT BITCHES");
                                String input1 = ((TextView) getActivity().findViewById(R.id.wc_input_1)).getText().toString();//store rather 1
                                String input2 = ((TextView) getActivity().findViewById(R.id.wc_input_2)).getText().toString();//store rather 2
                                ((TextView) getActivity().findViewById(R.id.wc_input_1)).setText("");//clear
                                ((TextView) getActivity().findViewById(R.id.wc_input_2)).setText("");//clear
                                //when submit hit or time run out
                                getDataFromUsers(input1, input2);//parse and store the data
                                Log.i(TAG, "I MADE IT PAST BITCHES");
                            }
                        };
                        getActivity().setContentView(R.layout.wouldchuck_input);
                        handler2.postDelayed(r2, 30000);
                    }
                };
                handler1.postDelayed(r, 2500);
            }
        });
    }

    public void getDataFromUsers(String input1, String input2) {
        Log.i(TAG, "DATA BITCHES");
        responses = new String[players.size()][2]; //here so if players drop out we dont expect stuff from them. also clears array
        Arrays.fill(responses, new String[]{"", ""});
        recieved = new ArrayList<Boolean>();

        for (int i = 0; i < responses.length; i++) {
            recieved.add(false);
        }
        Log.i(TAG, "RESPONSES");
        responses[0][0] = input1;//SET THE HOSTS STUFF
        responses[0][1] = input2;
        recieved.set(0, true);
        Log.i(TAG, "HOST DATA");

        if (host) {
            Log.i(TAG, "HOST");
            while (true) {
                if (recieved.indexOf(false) == -1) {
                    stage = 2;
                    Log.i(TAG, "BREAK-H");
                    break;
                }
            }
            BluetoothService.writeToClients(Constants.NEXT, new String[]{"true"});

        } else {
            Log.i(TAG, "CLIENT");
            BluetoothService.writeToServer("" + me.playerID, Constants.WC_SUBMISSION, new String[]{input1, input2});
            Log.i(TAG, me.playerID + ": WROTE TO HOST: " + input1 + ", " + input2);
            while (true) {
                if (cont) {
                    Log.i(TAG, "BREAK-C");
                    stage = 2;
                    break;
                }
            }
            cont = false;
        }
    }

    public void playerVoting() {
        stage = 3;
        if (host) {
            rathers = selectRathers(); //generate rathers
            for (int i = 0; i < recieved.size(); i++) { //clear the recieved array
                recieved.set(i, false);
            }
            vtePlayer1 = Integer.parseInt(rathers[2]);//store the owner ints //HOSTS ONLY
            vtePlayer2 = Integer.parseInt(rathers[3]);//store the owner ints
            rathers = new String[]{rathers[0], rathers[1]};//remove the ints

            BluetoothService.writeToClients(Constants.WC_QUESTION, rathers);//send the data over to clients
            while (true) {
                if (recieved.indexOf(false) == -1) { //loop until all have sent recieved
                    BluetoothService.writeToClients(Constants.NEXT, new String[]{"NEXT"});//time to continue
                    break;
                }
            }
        } else {
            while (true) {
                if (cont) {
                    cont = false;
                    break;
                }
            }
        }

        final boolean[] input = new boolean[2];

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setContentView(R.layout.wouldchuck_choice);
            }
        });

        Button ch1 = (Button) getActivity().findViewById(R.id.wc_choice_1);
        Button ch2 = (Button) getActivity().findViewById(R.id.wc_choice_2);

        //NEED TO DELAY THIS FOR X TIME
        if (vtePlayer1 == me.playerID || vtePlayer2 == me.playerID) {
            //grey out options if one of rathers is yours
            ch1.setEnabled(false);
            ch2.setEnabled(false);

        } else {
            //do nothing
        }


        ch1.setText(rathers[0]); //set option 1
        ch2.setText(rathers[1]); //set option 2

        //NEED TIME LOOPED
        getActivity().findViewById(R.id.wc_choice_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == getActivity().findViewById(R.id.wc_choice_1)) {
                    input[0] = true;
                    input[1] = false;
                } else {
                    input[1] = true;
                    input[0] = false;
                }
            }
        });

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                cont = true;
            }
        }, 30000);

        while (true) {
            if (cont) {
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

        getUserVotes(input);
    }

    public String[] selectRathers() {
        int randPlayer1, randPlayer2;
        int randQ1, randQ2;
        Random rand = new Random();

        while (true) {
            randPlayer1 = rand.nextInt(responses.length);
            randPlayer2 = rand.nextInt(responses.length);
            if (randPlayer1 == randPlayer2) {
                //loop again
            } else {
                int tmp1 = responsesRemaining(randPlayer1);
                int tmp2 = responsesRemaining(randPlayer2);
                if (tmp1 != 0 && tmp2 != 0) {
                    randQ1 = rand.nextInt(tmp1);
                    randQ2 = rand.nextInt(tmp2);

                    updateResponses(randPlayer1, randQ1);
                    updateResponses(randPlayer2, randQ2);

                    return new String[]{responses[randPlayer1][randQ1], responses[randPlayer2][randQ2], "" + randPlayer1, "" + randPlayer2};
                } else {
                    //loop again
                }
            }
        }
    }

    public int responsesRemaining(int index) { //returns the number of responses a user has left
        int i = 2;
        if (responses[index][0].equals(""))
            i--;
        if (responses[index][1].equals(""))
            i--;

        return i;
    }

    public void updateResponses(int index, int response) { //if the user gave a response, shift so Response[user][0] always has a response (makes rand easier)
        if (response == 1) {
            responses[index][1] = "";
        } else {
            String tmp = responses[index][1];
            responses[index][1] = "";
            responses[index][0] = tmp;
        }
    }

    public void getUserVotes(boolean[] vteResults) {
        clearRecieved();
        if (host) {
            while (true) {
                if (recieved.indexOf(false) == -1) {
                    BluetoothService.writeToClients(Constants.NEXT, new String[]{"Votes are in"});
                    break;
                }
            }
        } else {
            BluetoothService.writeToServer("" + me.playerID, Constants.WC_VOTE, new String[]{Boolean.toString(vteResults[0]), Boolean.toString(vteResults[1])});
            while (true) {
                if (cont) {
                    cont = false;
                    break;
                }
            }
        }
    }

    public void showVotingResults() {
        clearRecieved();
        if (host) {
            BluetoothService.writeToClients(Constants.WC_RESULTS, new String[]{"" + vtePlayer1, "" + vtePlayer2, "" + votes1, "" + votes2});
            while (true) {
                if (recieved.indexOf(false) == -1) {
                    BluetoothService.writeToClients(Constants.NEXT, new String[]{"Cont to Points"});
                    break;
                }
            }
        } else {
            while (true) {
                if (cont) {
                    cont = false;
                    break;
                }
            }
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setContentView(R.layout.wouldchuck_results);
            }
        });

        ((TextView) getActivity().findViewById(R.id.wc_results_content1)).setText(rathers[0]);
        ((TextView) getActivity().findViewById(R.id.wc_results_content2)).setText(rathers[1]);
        ((TextView) getActivity().findViewById(R.id.wc_results_count1)).setText("" + votes1);
        ((TextView) getActivity().findViewById(R.id.wc_results_count2)).setText("" + votes2);
        ((TextView) getActivity().findViewById(R.id.wc_results_user1)).setText(players.get(vtePlayer1).username);
        ((TextView) getActivity().findViewById(R.id.wc_results_user1)).setText(players.get(vtePlayer2).username);

        if (votes1 > votes2) {
            ((TextView) getActivity().findViewById(R.id.wc_results_score1)).setText("" + (500 * gameRound));
            ((TextView) getActivity().findViewById(R.id.wc_results_score2)).setText("0");
        } else {
            ((TextView) getActivity().findViewById(R.id.wc_results_score1)).setText("0");
            ((TextView) getActivity().findViewById(R.id.wc_results_score2)).setText("" + (500 * gameRound));
        }

        cont = false;

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                cont = true;
            }
        }, 30000);
        while (true) {
            if (cont) {
                cont = false;
                break;
            }
        }
        savePlayerPoints();
    }

    public void savePlayerPoints() {

        int points = gameRound * 500;
        int winner;

        if (votes1 > votes2) {
            winner = vtePlayer1;
        } else if (votes1 < votes2) {
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
    }

    public void showRoundResults() {
        int[][] order = playerOrder();

        //Show round splash screen
        View theInflatedView = inflater.inflate(R.layout.wouldchuck_round, null); //Show Intro frag
        //NEED TO DELAY THIS FOR X TIME
        for (int i = 0; i < order.length; i++) {
            ((TextView) getActivity().findViewById(getResources().getIdentifier("wc_leaderboard_name" + i, "id", "paperprisoners.couchpotato"))).setText(players.get(order[i][0]).username); //SEARCH STRING TO RESOURCE ID
            ((TextView) getActivity().findViewById(getResources().getIdentifier("wc_leaderboard_score" + i, "id", "paperprisoners.couchpotato"))).setText("" + order[i][1]);
        }

        theInflatedView.setVisibility(View.GONE);//close Intro frag

    }

    public int[][] playerOrder() {
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
    }

    public void gameOver() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setContentView(R.layout.activity_select);
            }
        });
        /*if (host) {
            /*
            show play again option
            if(play again){


            Thread temp = start;
            temp.start();//start a new "start" thread
            start.interrupt();//close current thread

            }
            else(){
             go to main menu
            }

        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().setContentView(R.layout.wouldchuck_waiting);
                }
            });
        }*/

    }

    //endregion
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
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

    @Override
    public void onReceiveMessage(int player, int messageType, Object[] content) {
        Log.i(TAG, "RECIEVED MESSAGE " + messageType);
        switch (messageType) {
            case Constants.NEXT:
                cont = true;
                break;
            case Constants.WC_SUBMISSION:
                //int index = findPlayerIndex(player);
                int index = 1;
                responses[index][0] = (String) content[0];
                responses[index][1] = (String) content[1];
                recieved.set(index, true);
                break;
            case Constants.WC_QUESTION:
                vtePlayer1 = Integer.parseInt((String) content[2]);//store the owner ints
                vtePlayer2 = Integer.parseInt((String) content[3]);//store the owner ints
                rathers = new String[]{(String) content[0], (String) content[1]};//remove the ints
                BluetoothService.writeToServer(me.playerID + "", Constants.MESSAGE_READ, new String[]{"Got Questions"});
                break;
            case Constants.MESSAGE_READ:
                recieved.set(player, true);
                break;
            case Constants.WC_VOTE:
                recieved.set(player, true);

                if (Boolean.valueOf((String) content[0]) == true) {
                    votes1++;
                } else {
                    votes2++;
                }
                break;
            case Constants.WC_RESULTS:
                vtePlayer1 = Integer.parseInt((String) content[0]);//store the owner ints
                vtePlayer2 = Integer.parseInt((String) content[1]);//store the owner ints
                votes1 = Integer.parseInt((String) content[2]);//store the owner ints
                votes2 = Integer.parseInt((String) content[3]);//store the owner ints
                BluetoothService.writeToServer("" + me.playerID, Constants.NEXT, new String[]{"voting results recieved"});
                break;

        }
    }

    public int findPlayerIndex(int id) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).playerID == id) {
                return i;
            }
        }
        return -1;
    }

    public void loading() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setContentView(R.layout.wouldchuck_waiting);
                ((TextView) getActivity().findViewById(R.id.wc_waiting_text)).setText(getResources().getString(R.string.waitingL));
            }
        });
    }

    public void clearRecieved() {
        for (int i = 0; i < recieved.size(); i++) {
            recieved.set(i, false);
        }
    }
}
