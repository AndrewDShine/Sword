import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.Timer;

import sun.invoke.empty.Empty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

@SuppressWarnings({ "serial", "unused" })
public class SwordRunner extends JPanel
	{
		public Player guy = new Player(new Vector(-40,-40), "player");
		public final int maxLives = 3;
		public Ghost guyLives = new Ghost(new Vector(5,10), "life");
		public Ghost lostLives = new Ghost(new Vector(5, 10), "death");
		public static ArrayList<ArrayList<Block>> level;
		public ArrayList<Enemy> goombas = new ArrayList<Enemy>();
		public ArrayList<Boss> bosses = new ArrayList<Boss>();
		public Vector levelVel = new Vector(0,0);
		public String xDir = "";
		public String lastDir = "r";
		public boolean isJumping = false;
		public final int size = 40;
		public Entity skybox1;
		public Entity skybox2;
		public int levelNum = 3;
		
		public static void main(String[] args)
			{
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				JFrame frame = new JFrame("Sword");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(1000, 880);
				SwordRunner game = new SwordRunner();
				frame.add(game);
				frame.setVisible(true);
				frame.setResizable(false);
				game.setFocusable(true);
				frame.setLocation((int)(screenSize.getWidth() / 2) - 600, (int)(screenSize.getHeight() / 2) - 480);
				
			}
		public SwordRunner()
		{
			setBackground(Color.CYAN);
			addKeyListener(new KeyAdapter()
					{
						@Override
						public void keyPressed(KeyEvent e)
						{
							switch(e.getKeyCode())
							{
								case KeyEvent.VK_RIGHT:
								case KeyEvent.VK_D:
									xDir = "r";
									break;
								case KeyEvent.VK_LEFT:
								case KeyEvent.VK_A:
									xDir = "l";
									break;
								case KeyEvent.VK_UP:
								case KeyEvent.VK_W:
									isJumping = true;
									break;
								default:
									xDir = "";
									guy.setCurrFrame(0);
									break;
							}
						}
						public void keyReleased(KeyEvent e)
						{
							switch(e.getKeyCode())
							{
								case KeyEvent.VK_RIGHT:
								case KeyEvent.VK_D:
									xDir = "";
									lastDir = "r";
									break;
								case KeyEvent.VK_LEFT:
								case KeyEvent.VK_A:
									lastDir = "l";
									xDir = "";
									break;
								case KeyEvent.VK_UP:
								case KeyEvent.VK_W:
									isJumping = false;
									break;
							}
						}
					});
			readLevel();
			Timer timer = new Timer(10, new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					if(xDir.equals("r"))
						{
							guy.getVel().setX(5);
						}
					else if(xDir.equals("l"))
						{
							guy.getVel().setX(-5);
						}
					else
						{
							guy.getVel().setX(0); 
						}
					if(isJumping)
						{
							if(guy.checkEverything()[0])
								guy.getVel().setY(-20);
						}
					playerTick();
					enemyTick();
					bossTick();
					repaint();
				}
			});
			timer.start();
			Timer animTimer = new Timer(125, new ActionListener(){
				public void actionPerformed(ActionEvent arg0)
					{
						for(Enemy e: goombas)
							{
							if(e.getCurrFrame()<e.getMaxFrames())
								{
								e.setCurrFrame(e.getCurrFrame()+1);
								if(e.getCurrFrame()==e.getMaxFrames())
									e.setCurrFrame(0);
								}
							}
						if(guy.getCurrFrame()<guy.getMaxFrames()&&!(xDir.equals("")))
							{
								guy.setCurrFrame(guy.getCurrFrame()+1);
								if(guy.getCurrFrame()==guy.getMaxFrames())
									guy.setCurrFrame(0);
							}
						else
							guy.setCurrFrame(0);
					}
			});
			animTimer.start();
		}
		
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g.drawImage(skybox1.getImage(), skybox1.getPos().getX(), skybox1.getPos().getY(), null);
			g.drawImage(skybox2.getImage(), skybox2.getPos().getX(), skybox2.getPos().getY(), null);
			for(int r = level.size() - 1; r >= 0; r--)
				{
					for(int c = 0; c < level.get(r).size(); c++)
						{
							if(level.get(r).get(c) != null)
								{
									if(!level.get(r).get(c).getType().equals(""))
										{
											g.drawImage(level.get(r).get(c).getImage(), level.get(r).get(c).getPos().getX(), level.get(r).get(c).getPos().getY(), null);	
										}
									else
										{	
//											g.setColor(level.get(r).get(c).getColor());
//											g.fillRect(level.get(r).get(c).getPos().getX(), level.get(r).get(c).getPos().getY(), size, size);
										}	
//									g.setColor(level.get(r).get(c).getColor());
//									g.fillRect(level.get(r).get(c).getPos().getX(), level.get(r).get(c).getPos().getY(), size, size);
								}
						}
				}
			for(Enemy e: goombas)
				{
					if(e.getVel().getX() > 0)
						g.drawImage(e.getAnim().get(e.getCurrFrame()), e.getPos().getX(), e.getPos().getY(), e.getPos().getX() + 40, e.getPos().getY() + 40, 0, 0, 40, 40, null, null);
					else if(e.getVel().getX() < 0)
						g.drawImage(e.getAnim().get(e.getCurrFrame()), e.getPos().getX(), e.getPos().getY(), e.getPos().getX() + 40, e.getPos().getY() + 40, 40, 0, 0, 40, null, null);
				}
			for(Boss b: bosses)
				{
					g.drawImage(b.getAnim().get(b.getCurrFrame()), b.getPos().getX(), b.getPos().getY(), b.getPos().getX() + 80, b.getPos().getY() + 80, 0, 0, 80, 80, null, null);
				}
			
			if(guy.getVel().getX() > 0)
				g.drawImage(guy.getAnim().get(guy.getCurrFrame()), guy.getPos().getX(), guy.getPos().getY(), guy.getPos().getX() + 40, guy.getPos().getY() + 40, 0, 0, 40, 40, null, null);
			else if(guy.getVel().getX() < 0)
				g.drawImage(guy.getAnim().get(guy.getCurrFrame()), guy.getPos().getX(), guy.getPos().getY(), guy.getPos().getX() + 40, guy.getPos().getY() + 40, 40, 0, 0, 40, null, null);
			else if(guy.getVel().getX() == 0 && lastDir.equals("r"))
				g.drawImage(guy.getAnim().get(guy.getCurrFrame()), guy.getPos().getX(), guy.getPos().getY(), guy.getPos().getX() + 40, guy.getPos().getY() + 40, 0, 0, 40, 40, null, null);
			else if(guy.getVel().getX() == 0 && lastDir.equals("l"))
				g.drawImage(guy.getAnim().get(guy.getCurrFrame()), guy.getPos().getX(), guy.getPos().getY(), guy.getPos().getX() + 40, guy.getPos().getY() + 40, 40, 0, 0, 40, null, null);
			
			for(int i = 0; i < guy.getLives(); i++)
				{
					g.drawImage(guyLives.getImage(), guyLives.getPos().getX() + (45 * i), guyLives.getPos().getY(), null);
				}
			for(int i = 0; i < maxLives - guy.getLives(); i++)
				{
					g.drawImage(lostLives.getImage(), (45 * guy.getLives()) + (45 * i) + lostLives.getPos().getX(), lostLives.getPos().getY(), null);
				}
			for(Boss b: bosses)
				for(int i = 0; i < b.getHearts().size(); i++)
					g.drawImage(b.getHearts().get(i).getImage(), b.getHearts().get(i).getPos().getX(), b.getHearts().get(i).getPos().getY(), null);
			for(Boss b: bosses)
				for(Rectangle r: b.getHitBoxes())
					g.drawRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
		}
		public void readLevel()
		{
			level = new ArrayList<ArrayList<Block>>();
			Scanner levelReader = null;
			int position = 0;
			try
				{
					levelReader = new Scanner(new File("level" + levelNum +".txt"));
				} catch (FileNotFoundException e)
				{
//					System.out.println("Level not found");
					System.out.println("BET");
				}
			int x = size * -1;
			int y = 0;
			while(levelReader.hasNextLine())
				{
					ArrayList<Block> newLine = new ArrayList<Block>();
					String levelLine = levelReader.nextLine();
					char[] levelArray = levelLine.toCharArray();
					for(char c: levelArray)
						{
							Block b;
							switch(c)
							{
								case 'g':
									b = new Block(new Vector(x,y), Color.GREEN, "grass");
									newLine.add(b);
									break;
								case 'r':
									b = new Block(new Vector(x,y), Color.RED, "dirt");
									newLine.add(b);
									break;
								case 'w':
									newLine.add(new Block(new Vector(x,y), Color.CYAN, ""));
									break;
								case 'p':
									guy.setPos(new Vector(x, y));
									break;
								case 'e':
									goombas.add(new Enemy(new Vector(x,y), "enemy"));
									break;
								case 'c':
									b = new Block(new Vector(x,y), Color.CYAN, "cloud");
									newLine.add(b);
									break;
								case 'x':
									b = new Block(new Vector(x,y), Color.RED, "end");
									newLine.add(b);
									break;
								case 'b':
									bosses.add(new Boss(new Vector(x,y)));
									for(int k = 0; k < 3; k++)
										bosses.get(bosses.size() - 1).getHearts().add(new Ghost(new Vector(x,y), "bossLife"));
									newLine.add(null);
									break;
								case ' ':
									newLine.add(null);
									break;
								case 't':
									b = new Block(new Vector(x,y), Color.GRAY, "stone");
									newLine.add(b);
									break;
							}
							x += size;
						}
					level.add(newLine);
					x = size * -1;
					y += size;
				}
			try
				{
					
			skybox1 = new Entity(new Vector(0,0), "");
			skybox1.setImage(ImageIO.read(new File("src/Images/grasslandskybox1.png")));
			
			skybox2 = new Entity(new Vector(1000,0), "");
			skybox2.setImage(ImageIO.read(new File("src/Images/grasslandskybox2.png")));
			
				}
			catch(Exception e){}
			
		}

		public void tick(int incX, int incY)
		{
			for(ArrayList<Block> line: level)
				{
					for(Block b: line)
						{
							if(b != null)
								{
									b.getPos().setX(b.getPos().getX() - incX);
									b.getPos().setY(b.getPos().getY() + incY);
								}
						}
				}
			for(Enemy e: goombas)
				{
					e.getPos().setX(e.getPos().getX() - incX);
					e.getPos().setY(e.getPos().getY() + incY);
				}
			for(Boss b: bosses)
				{
					b.getPos().setX(b.getPos().getX() - incX);
					b.getPos().setY(b.getPos().getY() + incY);
					b.updateHitBoxes();
				}
			skybox1.setCounter(skybox1.getCounter()+1);
			if(skybox1.getCounter()==3)
				{
					if(skybox1.getPos().getX()<-1000)
						skybox1.getPos().setX(999);
					if(skybox2.getPos().getX()<-1000)
						skybox2.getPos().setX(999);
					
					if(skybox1.getPos().getX()>1000)
						skybox1.getPos().setX(-999);
					if(skybox2.getPos().getX()>1000)
						skybox2.getPos().setX(-999);
					
					skybox1.getPos().setX(skybox1.getPos().getX() - incX);
					skybox2.getPos().setX(skybox2.getPos().getX() -incX);
					skybox1.setCounter(0);
				}
		}
		public void playerTick()
		{
			boolean[] checks;
			for(int i = 0; i < Math.abs(guy.getVel().getX()); i++)
				{
					checks = guy.checkEverything();
					if(!checks[1])
						{
							int increment = guy.getVel().getX() / Math.abs(guy.getVel().getX());
							if(checks[4])
								tick(increment, 0);
							else
								guy.getPos().setX(guy.getPos().getX() + increment);
						}
					guy.getLeftB().setLocation(guy.getPos().getX() - 1, guy.getPos().getY());
					guy.getRightB().setLocation(guy.getPos().getX() + size, guy.getPos().getY());
					guy.getHitBoxes()[4].setLocation(guy.getPos().getX(), guy.getPos().getY());
				}
			for(int i = 0; i < Math.abs(guy.getVel().getY()); i++)
				{
					for(Boss b: bosses)
						{
							String collideCheck = checkEnemyCollide(b);
							if(collideCheck.equals("bounce"))
								{
									guy.getVel().setY(-15);
									break;
								}
						}
					checks = guy.checkEverything();
					if(!checks[0] || guy.getVel().getY() < 0)
						{
							if(checks[2])
								{
									guy.getVel().setY(0);
								}
							if(guy.getVel().getY() != 0)
								{
									int increment = guy.getVel().getY() / Math.abs(guy.getVel().getY());
									guy.getPos().setY(guy.getPos().getY() + increment);
								}
						}
					else if(checks[0])
						{
							guy.getVel().setY(0);
							break;
						}
					guy.getUpB().setLocation(guy.getPos().getX(), guy.getPos().getY() - 1);
					guy.getDownB().setLocation(guy.getPos().getX(), guy.getPos().getY() + size);
					guy.getHitBoxes()[4].setLocation(guy.getPos().getX(), guy.getPos().getY());
//					checkEnemyCollide();
				}
			guy.getLeftB().setLocation(guy.getPos().getX() - 1, guy.getPos().getY());
			guy.getRightB().setLocation(guy.getPos().getX() + size, guy.getPos().getY());
			guy.getUpB().setLocation(guy.getPos().getX(), guy.getPos().getY() - 1);
			guy.getDownB().setLocation(guy.getPos().getX(), guy.getPos().getY() + size);
			guy.getHitBoxes()[4].setLocation(guy.getPos().getX(), guy.getPos().getY());
			
			checks = guy.checkEverything();
			if(!checks[0] && guy.getVel().getY() < 15)
				{
					guy.getVel().setY(guy.getVel().getY() + 1);
				}
			if(checks[3])
				{
					levelNum++;
					readLevel();
				}
		}
		public void enemyTick()
		{
			boolean shouldReset = false;
			ArrayList<Enemy> gc = new ArrayList<Enemy>();
			for(Enemy e: goombas)
				{
					for(int i = 0; i < Math.abs(e.getVel().getX()); i++)
						{
							String collideCheck = checkEnemyCollide(e);
							if(collideCheck.substring(0, 5).equals("death"))
								{
									gc.add(e);
									getHurt(collideCheck.substring(5));
									break;
								}
							else if(collideCheck.equals("bounce"))
								{
									gc.add(e);
									guy.getVel().setY(-15);
									break;
								}
							boolean[] checks = e.checkEverything();
							if(!checks[1])
								{
									int increment = e.getVel().getX() / Math.abs(e.getVel().getX());
									e.getPos().setX(e.getPos().getX() + increment);
								}
							else
								{
									e.getVel().setX(e.getVel().getX() * -1);
								}
							e.getLeftB().setLocation(e.getPos().getX() - 1, e.getPos().getY());
							e.getRightB().setLocation(e.getPos().getX() + size, e.getPos().getY());
						}
				}
			goombas.removeAll(gc);
//			if(shouldReset)
//				deathReset();
		}
		public void bossTick()
		{
			ArrayList<SwordObject> gc = new ArrayList<SwordObject>();
			for(Boss b: bosses)
				{
					boolean[] checks = b.checkEverything();
					if(!checks[0])
						{
							b.setCurrFrame(1);
							if(b.getVel().getY() <= 5)
								b.getVel().setY(b.getVel().getY() + 1);
						}
					else
						{
							b.setCurrFrame(0);
						}
					for(int i = 0; i < Math.abs(b.getVel().getY()); i++)
						{
							if(b.getVel().getY() != 0)
								{
									int increment = b.getVel().getY() / Math.abs(b.getVel().getY());
									b.getPos().setY(b.getPos().getY() + increment);
									b.updateHitBoxes();
								}
							if(b.checkEverything()[0])
								{
									b.getVel().setY(0);
									break;
								}
						}
					int rand = (int) ((Math.random() * 100) + 1);
					if(rand == 55 && b.checkEverything()[0])
						{
							b.getVel().setY(-20);
							if(guy.getPos().getX() - b.getPos().getX() < 0)
								b.setJumpingRight(false);
							else if(guy.getPos().getX() - b.getPos().getX() > 0)
								b.setJumpingRight(true);
						}
					if(b.isJumpingRight())
						b.getVel().setX(4);
					else
						b.getVel().setX(-4);
					for(int i = 0; i < Math.abs(b.getVel().getX()); i++)
						{
							if(b.checkEverything()[1])
								{
									b.getVel().setX(0);
									break;
								}
							if(b.getVel().getX() != 0 && b.getVel().getY() != 0)
								{
									int increment = b.getVel().getX() / Math.abs(b.getVel().getX());
									b.getPos().setX(b.getPos().getX() + increment);
									b.updateHitBoxes();
								}
						}
					for(int g = 0; g < b.getHearts().size(); g++)
						{
							Ghost l = b.getHearts().get(g);
							l.getPos().setX(b.getPos().getX() + 8 + (21 * g));;
							l.getPos().setY(b.getPos().getY() - 15);
						}
				}
		}
		public String checkEnemyCollide(Enemy e)
		{
			for(Rectangle r: e.getHitBoxes())
				for(Rectangle h: guy.getHitBoxes())
					{
						if(r.intersects(h) && !h.equals(guy.getDownB()) && h.equals(guy.getRightB()))
							return "deathl";
						else if(r.intersects(h) && !h.equals(guy.getDownB()) && h.equals(guy.getLeftB()))
							return "deathr";
						else if(r.intersects(h) && h.equals(guy.getDownB()))
							return "bounce";
					}
					
			return "nooooooo";
		}
		public void getHurt(String dir)
		{
			guy.setLives(guy.getLives() - 1);
			guy.getVel().setY(-10);
			if(dir.equals("l"))
				guy.getVel().setX(-10);
			else if(dir.equals("r"))
				guy.getVel().setX(10);
		}
	}