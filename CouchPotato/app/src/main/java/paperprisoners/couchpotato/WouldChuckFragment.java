package paperprisoners.couchpotato;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

public class WouldChuckFragment extends Fragment {
    Handler handler = new Handler();
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //GLOBAL VARS
    private boolean host = false;
    private int gameRound;
    private TextView clock;
    private String[][] responses;
    private String[] rathers;
    private int responsesLeft;
    private int vtePlayer1, vtePlayer2;//store the oners of votes1 and votes2
    private int votes1, votes2;
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private Thread start;

    @Override
    public synchronized void onStart() {
        super.onStart();
        clock = (TextView) getView().findViewById(R.id.clock);

        //*ADD END GAME METHOD FOR IF LOBBY NEEDS TO QUIT NOW*

        start = new Thread() {
            @Override
            public void run() {
                try {
                    gameRound = 1;

                    //Show Intro frag
                    //close Intro frag
                    while (gameRound <= 3) {
                        //responsesLeft = playerNumber * 2;

                        usersEnterValues();
                        while (responsesLeft > 0) {
                            playerVoting();
                            showVotingResults();
                            responsesLeft = responsesLeft - 2;
                        }
                        if(gameRound != 3){
                            showRoundResults();
                        }
                        else{
                            showFinalResults();
                            break;
                        }
                        gameRound++;
                    }
                    gameOver();
                } catch (Exception e) {
                    Log.v("WC_ERROR", e.getMessage());
                }
            }
        };
        start.start();
    }

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //region Game Code
    public void countClockDownVisual(final int time, int delay) {
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
        //show frag with input options
        //when submit hit or time run out
        getDataFromUsers();
    }

    public void getDataFromUsers() {
        /*
        responses = new String[Number of Players][2]; //here so if players drop out we dont expect stuff from them. also clears array
        Array.fill(responses, "");

        responsesLeft = numPlayers * 2;

        if (host) {
            //store host's data
            while(waiting on users){
             if(data recieved){
                store data into responses

                responses[playerID][0] = response1;
                responses[playerID][0] = response2;
              }
            }
        }
        else {
            write to host
        }
        */
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

        boolean[] input = new boolean[2];

        if (/*one of values is user's*/) {
            //show selection screen , disable options
        } else {
            //show selection screen
            //close selection screen when timer runs out or input
            //true for selected value, false for other;
        }
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

    public void getUserVotes( boolean[] vteResults) {
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
        savePlayerPoints();
    }

    public void savePlayerPoints() {
        /*
        int points = gameRound * 500;
        int winner;
        if(vote1 > vote2){
            winner = vtePlayer1;
        }
        else if (vote1 < vote2{
            winner = vtePlayer2;
        }
        else{
            winner = -1;
            points = points / 2;
        }
        if(winner == -1){
         playerList[vtePlayer1].setPoints(playerList[winner].getPoints() + points);
         playerList[vtePlayer2].setPoints(playerList[winner].getPoints() + points);
        }
        else{
         playerList[winner].setPoints(playerList[winner].getPoints() + points);
        }
         */
    }

    public void showRoundResults() {
        int[][] order = playerOrder();

        //Show round splash screen

    }

    public int[][] playerOrder() {
        int[][] order = new int[responses.length][2];

        /*
        for(int i = 0; i < numPlayers; i++){
            order[i][0] = playerList.get(i).getID();
            order[i][1] = playerList.get(i).getPoints();
        }

        int tempID, tempPoints;
        for (int i = 1; i < order.length; i++) { //sort by points (highest to lowest)
            for(int j = i ; j > 0 ; j--){
                if(order[j][1] > order[j-1][1]){
                    tempID = input[j][0];
                    tempPoints = input[j][1];

                    order[j][0] = order[j-1][0];
                    order[j][1] = order[j-1][1];

                    input[j-1][0] = tempID;
                    input[j-1][1] = tempPoints
                }
            }
        }
        */
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
