package uwaterloo.ca.lab4_205_03;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by nathanielruiz98 on 2017-06-29.
 */

abstract public class GameBlockTemplate extends ImageView {

    public abstract void setDestination();

    public abstract void move();

    GameBlockTemplate(Context myContext) {
        super(myContext);
    }
}
