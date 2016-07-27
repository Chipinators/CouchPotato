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
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class WouldChuckFragment extends Fragment {
    Handler handler = new Handler();
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //GLOBAL VARS
    private boolean host = false;
    private UserData me; //Will be passed to GameActivity, pull from there later
    private int gameRound;
    private TextView clock;
    private String[][] responses;
    private String[] rathers;
    private int responsesLeft;
    private int vtePlayer1, vtePlayer2;//store the oners of votes1 and votes2
    private int votes1, votes2;
    private LayoutInflater inflater;
    private ArrayList<UserData> players;
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private Thread start;

    @Override
    public synchronized void onStart() {
        super.onStart();
        clock = (TextView) getView().findViewById(R.id.clock);

        players = ((GameActivity) getActivity()).getPlayers(); //store the players
        me = ((GameActivity) getActivity()).getMe();

        TextView tv = (TextView) getActivity().findViewById(R.id.clock);
        tv.setText(me.username);
        //region Start Thread
        start = new Thread() {
            @Override
            public void run() {
                try {
                    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
                    gameRound = 1; //initalize the starting round
                    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
                    inflater = LayoutInflater.from(getActivity().getBaseContext()); //create the inflater
                    View theInflatedView = inflater.inflate(R.layout.wouldchuck_waiting, null); //Show Intro frag
                    //NEED TO DELAY THIS FOR X TIME
                    theInflatedView.setVisibility(View.GONE);//close Intro frag
                    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
                    while (gameRound <= 3) { //loop through the rounds!

                        responsesLeft = players.size() * 2; //number of responses

                        usersEnterValues();//get the user's rathers

                        while (responsesLeft > 0) { //loop through the rathers to let user's vote
                            playerVoting(); //playerID's cast votes
                            showVotingResults(); //results of vote are shown
                            responsesLeft = responsesLeft - 2; //update responses to pars
                        }
                        if (gameRound != 3) {//if the last round
                            showRoundResults();//show the final winner screen
                        } else {//otherwise
                            showFinalResults(); //show the end of round screen
                            break;
                        }
                        gameRound++;
                    }
                    gameOver();//call the game over screen!
                } catch (Exception e) {
                    Log.v("WC_ERROR", e.getMessage()); //ERROR (in case you fuck heads didn't know)
                }
            }
        };
        start.start(); //Run the game thread!
        //endregion

    }

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //region Game Code
    public void countClockDownVisual(final int time, int delay) { //IGNORE ME IM USELESS
        Runnable timer = new Runnable() {
            int t = time;

            @Override
            public void run() {
                if (t > 0) {
                    clock.setText("" + t);
                    clock.invalidate();
                    t--;
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(timer, delay * 1000);
    }

    public void usersEnterValues() {
        View theInflatedView = inflater.inflate(R.layout.wouldchuck_choice, null);  //show frag with input options
        //NEED TO DELAY THIS FOR X TIME
        theInflatedView.setVisibility(View.GONE);//close input frag

        String input1 = ((TextView) getActivity().findViewById(R.id.wc_input_1)).getText().toString();//store rather 1
        String input2 = ((TextView) getActivity().findViewById(R.id.wc_input_2)).getText().toString();//store rather 2
        ((TextView) getActivity().findViewById(R.id.wc_input_1)).setText("");//clear
        ((TextView) getActivity().findViewById(R.id.wc_input_2)).setText("");//clear
        //when submit hit or time run out
        getDataFromUsers(input1, input2);//parse and store the data
    }

    public void getDataFromUsers(String input1, String input2) {

        responses = new String[players.size()][2]; //here so if players drop out we dont expect stuff from them. also clears array
        Arrays.fill(responses, "");

        if (host) {
            responses[0][0] = input1;
            responses[0][1] = input2;

            //while(waiting on users){
            //if(data recieved){
            // store data into response
            //}
            //}
        } else {
            //  write to host
        }
    }

    public void playerVoting() {

        if (host) {
            rathers = selectRathers();
            //send data
        } else {
            //recieve rathers
        }
        vtePlayer1 = Integer.parseInt(rathers[2]);//store the owner ints
        vtePlayer2 = Integer.parseInt(rathers[3]);//store the owner ints
        rathers = new String[]{rathers[0], rathers[1]};//remove the ints

        final boolean[] input = new boolean[2];

        View theInflatedView = inflater.inflate(R.layout.wouldchuck_choice, null);  //show frag with voting options

        //NEED TO DELAY THIS FOR X TIME
        if (vtePlayer1 == me.playerID || vtePlayer2 == me.playerID) {


        } else {
            //show selection screen
            //close selection screen when timer runs out or input
            //true for selected value, false for other;
        }

        Button ch1 = (Button) getActivity().findViewById(R.id.wc_choice_1);
        Button ch2 = (Button) getActivity().findViewById(R.id.wc_choice_2);
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

        theInflatedView.setVisibility(View.GONE);//close input frag
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
        if (host) {
            /*
            while(waiting for users){
                if(data recieved){ //want data in form of bool, bool
                  if(data[0] == false && data[1] == false){
                   //no vote, was user's response
                  }
                  else if(data[0] == true){
                   votes1 ++;
                  }
                  else{
                  votes2 ++;
                  }
                }
            }
             */
        } else {
            //send response as voted = true, didnt vote = false I.e true,false or false, true;
        }
    }

    public void showVotingResults() {
        if (host) {
            //send data to all int, int (votes1, votes2)
        } else {
            //read sent data
            //votes1 = data[0]
            //votes2 = data[1];
        }
        //show results
        View theInflatedView = inflater.inflate(R.layout.wouldchuck_results, null); //Show Intro frag
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
        theInflatedView.setVisibility(View.GONE);//close Intro frag

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

    public void showFinalResults() {
        int[][] order = playerOrder();

        //show game end splash screen
    }

    public void gameOver() {
        if (host) {
            /*
            show play again option
            if(play again){
                */

            Thread temp = start;
            temp.start();//start a new "start" thread
            start.interrupt();//close current thread
                /*
            }
            else(){
             go to main menu
            }
             */
        } else {
            //wait for host's decision
        }

    }

    //endregion
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //region Default Fragment Methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
