package rocketmakeup;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import javax.sound.sampled.*;

 

public class RocketMakeUp extends JFrame implements Runnable {

    boolean animateFirstTime = true;
    Image image;
    Graphics2D g;

    
//variables for rocket. 
    Rocket rocket;

    double frameRate = 25.0;
    
    boolean gameOver;
    int timeCount;
    
    
    static RocketMakeUp frame;
    public static void main(String[] args) {
        frame = new RocketMakeUp();
        frame.setSize(Window.WINDOW_WIDTH, Window.WINDOW_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public RocketMakeUp() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.BUTTON1 == e.getButton()) {
                    //left button

// location of the cursor.
                    int xpos = e.getX();
                    int ypos = e.getY();

                }
                if (e.BUTTON3 == e.getButton()) {
                    //right button
                    reset();
                }
                repaint();
            }
        });

    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        repaint();
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {

        repaint();
      }
    });

        addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                
                if (gameOver)
                    return;
                
                if (e.VK_UP == e.getKeyCode()) {
                    rocket.IncreaseYSpeed(1);
                } else if (e.VK_DOWN == e.getKeyCode()) {
                    rocket.IncreaseYSpeed(-1);
                } else if (e.VK_LEFT == e.getKeyCode()) {
                    rocket.IncreaseXSpeed(-1);
                } else if (e.VK_RIGHT == e.getKeyCode()) {
                    rocket.IncreaseXSpeed(1);
                } else if (e.VK_SPACE == e.getKeyCode()) {
//add or modify enh 2.  Pass in the value of the rocket's rotAngle.
                    Missile.Create(rocket.getXPos(),rocket.getYPos(),rocket.getRotAngle());
                }
                repaint();
            }
        });
        init();
        start();
    }
    Thread relaxer;
////////////////////////////////////////////////////////////////////////////
    public void init() {
        requestFocus();
    }
////////////////////////////////////////////////////////////////////////////
    public void destroy() {
    }

 

////////////////////////////////////////////////////////////////////////////
    public void paint(Graphics gOld) {
        if (image == null || Window.xsize != getSize().width || Window.ysize != getSize().height) {
            Window.xsize = getSize().width;
            Window.ysize = getSize().height;
            image = createImage(Window.xsize, Window.ysize);
            g = (Graphics2D) image.getGraphics();
            Drawing.setDrawingInfo(g,this);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
//fill background
        g.setColor(Color.cyan);
        g.fillRect(0, 0, Window.xsize, Window.ysize);

        int x[] = {Window.getX(0), Window.getX(Window.getWidth2()), Window.getX(Window.getWidth2()), Window.getX(0), Window.getX(0)};
        int y[] = {Window.getY(0), Window.getY(0), Window.getY(Window.getHeight2()), Window.getY(Window.getHeight2()), Window.getY(0)};
//fill border
        g.setColor(Color.black);
        g.fillPolygon(x, y, 4);
// draw border
        g.setColor(Color.red);
        g.drawPolyline(x, y, 5);

        if (animateFirstTime) {
            gOld.drawImage(image, 0, 0, null);
            return;
        }


        Star.Draw();
        Missile.Draw();
        rocket.Draw();
   
        if (gameOver) {
            g.setColor(Color.white);
            g.setFont(new Font("Arial",Font.PLAIN,50));
            g.drawString("GAME OVER",60,250);              
        }
            
        gOld.drawImage(image, 0, 0, null);
    }

////////////////////////////////////////////////////////////////////////////
// needed for     implement runnable
    public void run() {
        while (true) {
            animate();
            repaint();
            double seconds = 1/frameRate;    //time that 1 frame takes.
            int miliseconds = (int) (1000.0 * seconds);
            try {
                Thread.sleep(miliseconds);
            } catch (InterruptedException e) {
            }
        }
    }
/////////////////////////////////////////////////////////////////////////
    public void reset() {
        timeCount = 0;
        gameOver = false;
        rocket = new Rocket();
        Star.Reset();
        Missile.Reset();
    }
/////////////////////////////////////////////////////////////////////////
    public void animate() {
        if (animateFirstTime) {
            animateFirstTime = false;
            if (Window.xsize != getSize().width || Window.ysize != getSize().height) {
                Window.xsize = getSize().width;
                Window.ysize = getSize().height;
            }
            reset();
        }

        if (gameOver)
            return;
        
        
        if (Star.CollideRocket(rocket))
        {
            gameOver = true;
        }
        Missile.CollideStars();

        if (timeCount % (int)(4*frameRate) == ((int)(4*frameRate)-1))
        {     
            Star.Create(rocket.getXSpeed());
        }       
        
        Star.Animate(rocket.getXSpeed());
        Missile.Animate();
        rocket.Animate();
        timeCount++;

    }

////////////////////////////////////////////////////////////////////////////
    public void start() {
        if (relaxer == null) {
            relaxer = new Thread(this);
            relaxer.start();
        }
    }
////////////////////////////////////////////////////////////////////////////
    public void stop() {
        if (relaxer.isAlive()) {
            relaxer.stop();
        }
        relaxer = null;
    }

}

///////////////////////////////////////////////////////////////

class Star {
    private static ArrayList<Star> stars = new ArrayList<Star>();     

    private int xPos;
    private int yPos;
    private int size;

    public static void Reset() {
        stars.clear();
    }
    
    public static void Create(int xspeed)
    {
        if (xspeed > 0) {
            Star star = new Star(Window.getWidth2());
            stars.add(star);        
        }
        else if (xspeed < 0) {
            Star star = new Star(0);
            stars.add(star);        
        }
    }    
    
    public static void Draw() {    
        for (Star star : stars)
            Drawing.drawCircle(Window.getX(star.xPos),Window.getYNormal(star.yPos),0.0,star.size,star.size,Color.yellow );
    }
        
    public static void Animate(int xSpeed) {
        for (int i=0;i<stars.size();i++) {
            stars.get(i).xPos -= xSpeed;
            
//Remove the star when it goes beyond the right or left border.
            if (stars.get(i).xPos < 0) {
                stars.remove(i);
                i--;
            }
            else if (stars.get(i).xPos > Window.getWidth2()) {
                stars.remove(i);
                i--;
            }
        }
    }    
    
    public static boolean CollideRocket(Rocket rocket) {
        for (Star star : stars) {
            if (star.xPos + star.size*10 > rocket.getXPos() && star.xPos - star.size*10 < rocket.getXPos() &&
            star.yPos + star.size*10 > rocket.getYPos() && star.yPos - star.size*10 < rocket.getYPos())
                return (true);
        }
        return (false);
    }
    
    public static Star CollideMissile(Missile missile) {
        for (Star star : stars) {
            if (star.xPos + star.size*10 > missile.getXPos() && star.xPos - star.size*10 < missile.getXPos() &&
            star.yPos + star.size*10 > missile.getYPos() && star.yPos - star.size*10 < missile.getYPos())
                return (star);
        }
        return (null);
    }
    
    public static void RemoveStar(Star star) {
        stars.remove(star);
    }
            
    public Star(int _xPos) {        
        xPos = _xPos;
        yPos = (int)(Math.random()*Window.getHeight2());
        size = (int)(Math.random()*3+1);
    }    
    
}

///////////////////////////////////////////////////////////////

class Missile {
    private static ArrayList<Missile> missiles = new ArrayList<Missile>();     

    private int xPos;
    private int yPos;
    private int xSpeed;
//add or modify enh 2.  Variable already added for you.
    private int ySpeed;
    private int rotAngle;
    public static void Reset() {
        missiles.clear();
    }
    
//add or modify enh 2.  Method already given to you.
   public static void Create(int _xPos,int _yPos,int _rotAngle)
    {       
//add or modify enh 2.  Local variables already added for you.
            int xSpeed = 4;
            int ySpeed = 0;
//add or modify enh 2.  Use _rotAngle to determine the values of xSpeed and ySpeed.            
            if (ySpeed < 0){
                 _rotAngle = 315;
            }
            else if (ySpeed > 0){
                 _rotAngle = 45;
            }
//add or modify enh 2.  Code already added for you.
         Missile missile = new Missile(xSpeed,ySpeed,_xPos,_yPos,_rotAngle);
            missiles.add(missile);        
    } 
    
    public static void CollideStars() {
        Star star = null;
        for (int i=0;i<missiles.size();i++) {
            if ((star = Star.CollideMissile(missiles.get(i))) != null) {
                missiles.remove(i);
                i--;
            }
            if (star != null)
                Star.RemoveStar(star);
        }        
    }
    
    public static void Draw() {    
        for (Missile missile : missiles)
//add or modify enh 2.  Rotate the missile.  
            Drawing.drawCircle(Window.getX(missile.xPos),Window.getYNormal(missile.yPos),missile.rotAngle,2.0,0.4,Color.yellow );
    }
        
    public static void Animate() {
        for (int i=0;i<missiles.size();i++) {
            missiles.get(i).xPos += missiles.get(i).xSpeed;
    //add or modify enh 2.  Update the y position of the missile.
               if (missiles.get(i).rotAngle == 315){
                  missiles.get(i).yPos +=  missiles.get(i).xSpeed;
               }
               else if (missiles.get(i).rotAngle == 45){
                     missiles.get(i).yPos -=  missiles.get(i).xSpeed;
               }
//Remove the missile when it goes past the left or right border.
            if (missiles.get(i).xPos < 0 || missiles.get(i).xPos > Window.getWidth2())
            {
                missiles.remove(i);
                i--;
            }
        }
    }    
//add or modify enh 2.  Method already given to you) { 
  public Missile(int _xSpeed,int _ySpeed,int _xPos,int _yPos,int _rotAngle) { 
        xSpeed = _xSpeed; 
//add or modify enh 2.  Set ySpeed.
        ySpeed = _ySpeed;
        xPos = _xPos;
        yPos = _yPos;
//add or modify enh 2.  Set rotAngle.
        rotAngle = _rotAngle;
    }    
    public int getXPos() {
        return (xPos);
    }
    public int getYPos() {
        return (yPos);
    }    

}
///////////////////////////////////////////////////////////////
class Rocket {
    private static int maxSpeed = 10;
    private int xPos;
    private int yPos;
    private int xSpeed;
    private int ySpeed;
//add or modify enh 1.  Variable already added for you.
    private int rotAngle;
    public Rocket() {      
//Initially place the rocket in the center.        
        xPos = Window.getWidth2()/2;
        yPos = Window.getHeight2()/2;
        xSpeed = 2;
        ySpeed = 0;
//add or modify enh 1.  Initialize the value of rotAngle.         
        rotAngle = 0;
    }
//add or modify enh 2.  Add the method getRotAngle() so other classes can access rotAngle.
public int getRotAngle(){
    return(rotAngle);
}
    public void Draw() {                   
//add or modify enh 1.  Rotate the rocket.
            Drawing.drawRocket(Window.getX(xPos),Window.getYNormal(yPos),rotAngle,1.0,0.7,Color.red );    
    }

    public int getXSpeed() {
        return (xSpeed);
    }
    public int getXPos() {
        return (xPos);
    }
    public int getYPos() {
        return (yPos);
    }
    public void IncreaseXSpeed(int speedInc) {
        xSpeed += speedInc;
        if (xSpeed < 2)
            xSpeed = 2;
   
        
//Limit the xSpeed of the rocket.        
        if (xSpeed > maxSpeed)
            xSpeed = maxSpeed;
        else if (xSpeed < -maxSpeed)
            xSpeed = -maxSpeed;
    }
    public void IncreaseYSpeed(int speedInc) {
        ySpeed += speedInc;
    }
    public void Animate() {
        yPos += ySpeed;
//add or modify enh 1.  Use ySpeed to determine the value of rotAngle.
        if (ySpeed > 0){
            rotAngle = 315;
        }
        else if (ySpeed < 0){
            rotAngle = 45;
        }
        else {
            rotAngle = 0;
        }

//Prevent the rocket from going beyond the top or bottom border.
        if (yPos > Window.getHeight2())
        {
            yPos = Window.getHeight2();
            ySpeed = 0;
        }
        if (yPos < 0)
        {
            yPos = 0;
            ySpeed = 0;            
        }        
    }
}


////////////////////////////////////////////////////////////////////////////

class Window {
    private static final int XBORDER = 60;
    
//    private static final int YBORDER = 20;
    
    private static final int TOP_BORDER = 40;
    private static final int BOTTOM_BORDER = 20;
    
    private static final int YTITLE = 30;
    private static final int WINDOW_BORDER = 8;
    static final int WINDOW_WIDTH = 2*(WINDOW_BORDER + XBORDER) + 600;
    static final int WINDOW_HEIGHT = YTITLE + WINDOW_BORDER + 600;
    static int xsize = -1;
    static int ysize = -1;


/////////////////////////////////////////////////////////////////////////
    public static int getX(int x) {
        return (x + XBORDER + WINDOW_BORDER);
    }

    public static int getY(int y) {
//        return (y + YBORDER + YTITLE );
        return (y + TOP_BORDER + YTITLE );
        
    }

    public static int getYNormal(int y) {
//          return (-y + YBORDER + YTITLE + getHeight2());
      return (-y + TOP_BORDER + YTITLE + getHeight2());
        
    }
    
    public static int getWidth2() {
        return (xsize - 2 * (XBORDER + WINDOW_BORDER));
    }

    public static int getHeight2() {
//        return (ysize - 2 * YBORDER - WINDOW_BORDER - YTITLE);
        return (ysize - (BOTTOM_BORDER + TOP_BORDER) - WINDOW_BORDER - YTITLE);
    }    
}

class Drawing {
    private static Graphics2D g;
    private static RocketMakeUp mainClassInst;

    public static void setDrawingInfo(Graphics2D _g,RocketMakeUp _mainClassInst) {
        g = _g;
        mainClassInst = _mainClassInst;
    }
////////////////////////////////////////////////////////////////////////////
    public static void drawRocket(int xpos,int ypos,double rot,double xscale,double yscale,Color color)
    {
        g.translate(xpos,ypos);
        g.rotate(rot  * Math.PI/180.0);
        g.scale( xscale , yscale );

        g.setColor(color);
        g.fillRect(-10,-10,20,20);
        int xvals[] = {10,10,20};
        int yvals[] = {10,-10,0};
        g.fillPolygon(xvals,yvals,xvals.length);

        g.scale( 1.0/xscale,1.0/yscale );
        g.rotate(-rot  * Math.PI/180.0);
        g.translate(-xpos,-ypos);
    }        
////////////////////////////////////////////////////////////////////////////
    public static void drawCircle(int xpos,int ypos,double rot,double xscale,double yscale,Color color)
    {
        g.translate(xpos,ypos);
        g.rotate(rot  * Math.PI/180.0);
        g.scale( xscale , yscale );

        g.setColor(color);
        g.fillOval(-10,-10,20,20);

        g.scale( 1.0/xscale,1.0/yscale );
        g.rotate(-rot  * Math.PI/180.0);
        g.translate(-xpos,-ypos);
    }

}

 

 

 

