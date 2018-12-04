package com.example.a69.forprojectactivity;


        import java.util.Random;

        import android.app.Activity;
        import android.app.AlertDialog;
        import android.content.DialogInterface;
        import android.content.res.Resources;
        import android.graphics.Color;
        import android.os.Bundle;
        import android.support.v4.app.NavUtils;
        import android.view.Gravity;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup.LayoutParams;
        import android.widget.GridView;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;


public class GameActivity extends Activity {

    //слова
    private String[] words;
    //рандом для выбора слова
    private Random rand;
    //загаданное слово
    private String currWord;
    //область содержащая ответ
    private LinearLayout wordLayout;
    //текстовове поле для каждой буквы ответа
    private TextView[] charViews;
    //кнопки буквы
    private GridView letters;
    //адаптер ля кнопки буквы
    private LetterAdapter ltrAdapt;
    //изображения частей тела
    private ImageView[] bodyParts;
    //количество частей
    private int numParts=6;
    //текуща часть
    private int currPart;
    //номер симвоал в слове
    private int numChars;
    //num correct so far
    private int numCorr;
    //help
    private AlertDialog helpAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //чтение массива ответов
        Resources res = getResources();
        words = res.getStringArray(R.array.words);

        //инициализируем рандом
        rand = new Random();
        //инициализируем слово
        currWord="";

        //получаем зону ответа
        wordLayout = (LinearLayout)findViewById(R.id.word);

        //получаем кнопки с буквами
        letters = (GridView)findViewById(R.id.letters);

        //получаем части тела
        bodyParts = new ImageView[numParts];
        bodyParts[0] = (ImageView)findViewById(R.id.head);
        bodyParts[1] = (ImageView)findViewById(R.id.body);
        bodyParts[2] = (ImageView)findViewById(R.id.arm1);
        bodyParts[3] = (ImageView)findViewById(R.id.arm2);
        bodyParts[4] = (ImageView)findViewById(R.id.leg1);
        bodyParts[5] = (ImageView)findViewById(R.id.leg2);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        //начинаем игру
        playGame();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_help:
                showHelp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //play a new game
    private void playGame(){

        //выбираем слово
        String newWord = words[rand.nextInt(words.length)];
        //выбираем слово, которе не было последним
        while(newWord.equals(currWord)) newWord = words[rand.nextInt(words.length)];
        //обновляем правильно слово
        currWord = newWord;

        //задаем  длину загаданного слова
        charViews = new TextView[currWord.length()];

        //удаляем, существующие буквы
        wordLayout.removeAllViews();

        //цикл по загаданному слову
        for(int c=0; c<currWord.length(); c++){
            charViews[c] = new TextView(this);
            //установка текущей буквы
            charViews[c].setText(""+currWord.charAt(c));
            //насройк лайаота
            charViews[c].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            charViews[c].setGravity(Gravity.CENTER);
            charViews[c].setTextColor(Color.WHITE);
            charViews[c].setBackgroundResource(R.drawable.letter_bg);
            //добавление на дисплей
            wordLayout.addView(charViews[c]);
        }

        //перезапускаем адаптер
        ltrAdapt=new LetterAdapter(this);
        letters.setAdapter(ltrAdapt);

        //начинаем с нуля
        currPart=0;
        //устонавливаем длинну слова для корректного выбора
        numChars=currWord.length();
        numCorr=0;

        //скрываем все части
        for(int p=0; p<numParts; p++){
            bodyParts[p].setVisibility(View.INVISIBLE);
        }
    }

    //метод нажатия на букву
    public void letterPressed(View view){
        //вычисляем какая буква была нажата
        String ltr=((TextView)view).getText().toString();
        char letterChar = ltr.charAt(0);
        //делаем ее невидимой
        view.setEnabled(false);
        view.setBackgroundResource(R.drawable.letter_down);
        //проверяем подходит ли буква
        boolean correct=false;
        for(int k=0; k<currWord.length(); k++){
            if(currWord.charAt(k)==letterChar){
                correct=true;
                numCorr++;
                charViews[k].setTextColor(Color.BLACK);
            }
        }
        //проверяем в случае, если была правильная
        if(correct){
            if(numCorr==numChars){
                //делаем не активными все такие буквы
                disableBtns();
                //даем пользователю знать, что он победил
                AlertDialog.Builder winBuild = new AlertDialog.Builder(this);
                winBuild.setTitle("ПОБЕДА");
                winBuild.setMessage("Вы победили!\n\nОтвет был::\n\n"+currWord);
                winBuild.setPositiveButton("Играть снова",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                GameActivity.this.playGame();
                            }});
                winBuild.setNegativeButton("Выход",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                GameActivity.this.finish();
                            }});
                winBuild.show();
            }
        }
        //проверяем если пользователь все еще должен угадывать
        else if(currPart<numParts){
            //показываем следующю часть
            bodyParts[currPart].setVisibility(View.VISIBLE);
            currPart++;
        }
        else{
            //юзер ошибся
            disableBtns();
            //даем поьзователь знать, что он проиграл
            AlertDialog.Builder loseBuild = new AlertDialog.Builder(this);
            loseBuild.setTitle("ПОРАЖЕНИЕ");
            loseBuild.setMessage("Вы проиграли!\n\nОтвет был:\n\n"+currWord);
            loseBuild.setPositiveButton("Попробуйте снова",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            GameActivity.this.playGame();
                        }});
            loseBuild.setNegativeButton("Выход",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            GameActivity.this.finish();
                        }});
            loseBuild.show();
        }
    }

    //делаем неактивными буквы
    public void disableBtns(){
        int numLetters = letters.getChildCount();
        for(int l=0; l<numLetters; l++){
            letters.getChildAt(l).setEnabled(false);
        }
    }

        //отображаем меню поомщи
    public void showHelp(){
        AlertDialog.Builder helpBuild = new AlertDialog.Builder(this);
        helpBuild.setTitle("Инфо");
        helpBuild.setMessage("Угадайте загаданное слово.\n\n"
                + "У вас есть 6 попыток прежде, чем игра закончится");
        helpBuild.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        helpAlert.dismiss();
                    }});
        helpAlert = helpBuild.create();
        helpBuild.show();
    }

}
