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

import java.util.Arrays;
import java.util.Random;

public class WouldChuck extends Fragment {
    Handler handler = new Handler();
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //GLOBAL VARS
    private boolean host = false;
    private int gameID = 1;
    private int gameRound = 1;
    private TextView clock;
    private String[][] Responses;
    private String[] rathers;
    private int questionSets, responsesLeft;
    private int vtePlayer1, vtePlayer2;//store the oners of votes1 and votes2
    private int votes1, votes2;

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private Thread start;

    @Override
    public synchronized void onStart() {
        super.onStart();
        clock = (TextView) getView().findViewById(R.id.clock);
        Arrays.fill(Responses, "");//initalize and fill the array with nothing

        //*ADD END GAME METHOD FOR IF LOBBY NEEDS TO QUIT NOW*

        start = new Thread() {
            @Override
            public void run() {
                try {
                    //Show Intro frag
                    //close Intro frag
                    while (gameRound <= 3) {
                        questionSets = 0;

                        usersEnterValues();
                        //get num of questionsets
                        while (questionSets > 0) {
                            playerVoting();
                            showVotingResults();
                        }
                        showRoundResults();
                        gameRound++;
                    }
                    showFinalResults();
                    gameOver();
                } catch (Exception e) {
                    Log.v("ERROR WITH WAITING", e.getMessage());
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
        questionSets = number of players;
        Responses = new String[Number of Players][2]; //here so if players drop out we dont expect stuff from them. also clears array
        Array.fill(Responses, "");

        responsesLeft = numPlayers * 2;

        if (host) {
            //store host's data
            while(waiting on users){
             if(data recieved){
                store data into Responses

                Responses[playerID][0] = response1;
                Responses[playerID][0] = response2;
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

        if (/*one of values is user's*/) {
            //show selection screen , disable options
        } else {
            //show selection screen
            //close selection screen when timer runs out or input
        }
        getUserVotes();
        ;
    }

    public String[] selectRathers() {
        int randPlayer1, randPlayer2;
        int randQ1, randQ2;
        Random rand = new Random();

        while (true) {
            randPlayer1 = rand.nextInt(Responses.length);
            randPlayer2 = rand.nextInt(Responses.length);
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

                    responsesLeft = responsesLeft - 2;

                    return new String[]{Responses[randPlayer1][randQ1], Responses[randPlayer2][randQ2], "" + randPlayer1, "" + randPlayer2};
                } else {
                    //loop again
                }
            }
        }
    }

    public int responsesRemaining(int index) { //returns the number of responses a user has left
        int i = 2;
        if (Responses[index][0].equals(""))
            i--;
        if (Responses[index][1].equals(""))
            i--;

        return i;
    }

    public void updateResponses(int index, int response) { //if the user gave a response, shift so Response[user][0] always has a response (makes rand easier)
        if (response == 1) {
            Responses[index][1] = "";
        } else {
            String tmp = Responses[index][1];
            Responses[index][1] = "";
            Responses[index][0] = tmp;
        }
    }

    public void getUserVotes() {
        if (host) {
            /*
            while(waiting for users){
                if(data recieved){ //want data in form of bool, bool
                  if(data[0] == true){
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
        int winner;
        if(vote1 > vote2){
            winner = vtePlayer1;
        }
        else{
            winner = vtePlayer2;
        }
        if(gameRound == 1){
         playerList[winner].setPoints(playerList[winner].getPoints() + 500);
        }
        else if(gameRound == 2){
         playerList[winner].setPoints(playerList[winner].getPoints() + 1000);
        }
        else{
         playerList[winner].setPoints(playerList[winner].getPoints() + 1500);
        }
         */
    }

    public void showRoundResults() {
        int[][] order = playerOrder();

        //Show round splash screen

    }

    public int[][] playerOrder() {
        int[][] order = new int[Responses.length][2];

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
