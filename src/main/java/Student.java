import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Student implements Serializable,Comparable<Student> {
    public int score;
    public String name;
    public String division;
    public int tries;
    public boolean solved = false;

    public Student( String nname, int sscore, String ddivision, int ttries){
        score = sscore;
        name = nname;
        division = ddivision;
        tries = ttries;
    }


    @Override
    public int compareTo(@NotNull Student o) {
        if(score==o.score) return name.compareTo(o.name);
        else return Integer.compare(score,o.score);
    }
}
