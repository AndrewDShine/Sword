import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.sound.sampled.LineEvent.Type;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

@SuppressWarnings({ "serial", "unused" })
public class SwordRunner extends JPanel
	{
		public Player guy = new Player(new Vector(40,-40), "player");
		public final int maxLives = 5;
		public Ghost guyLives = new Ghost(new Vector(5,10), "life");
		public Ghost lostLives = new Ghost(new Vector(5, 10), "death");
		public Ghost score = new Ghost(new Vector(860, 10), "coin");
		public int scoreTotal = 0;
		public static int scoreCounter = 0;
		public static ArrayList<ArrayList<Block>> level;
		public ArrayList<Enemy> gc = new ArrayList<Enemy>();
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
		public int stage = 0;
		public int tickNum = 0;
		public int letterNum = 0;
		public int colorNum = 0;
		public Color[] colors = {Color.WHITE, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.PINK};
		public BufferedImage[] skyboxes;
		
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
				SoundEffect.init();
			}
		public SwordRunner()
		{
			setBackground(Color.BLACK);
			addKeyListener(new KeyAdapter()
					{
						@Override
						public void keyPressed(KeyEvent e)
						{
							if(stage == 1)
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
							else if(e.getKeyCode() == KeyEvent.VK_ENTER)
								{
									if(stage == 0)
										{
											stage++;
											levelNum++;
											goombas.clear();
											readLevel();
											guy = new Player(new Vector(40,-40), "player");
											xDir = "";
											isJumping = false;
										}
									else if(stage == 2)
										{
											levelNum++;
											goombas.clear();
											readLevel();
											guy = new Player(new Vector(40,-40), "player");
											stage = 1;
											xDir = "";
											isJumping = false;
											scoreCounter = 0;
										}
									else if(stage == 3)
										{
											goombas.clear();
											bosses.clear();
											readLevel();
											guy = new Player(new Vector(40,-40), "player");
											stage = 1;
											xDir = "";
											isJumping = false;
											scoreCounter = 0;
										}
								}
						}
						public void keyReleased(KeyEvent e)
						{
							if(stage == 1)
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
						}
					});
			readLevel();
			Timer timer = new Timer(10, new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					switch(stage)
					{
						case 0:
							if(tickNum < 60)
								tickNum++;
							else
								{
									tickNum = 0;
									letterNum++;
								}
							enemyTick();
							repaint();
							break;
						case 1:
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
							break;
						case 2:
						case 3:
						case 4:
							if(tickNum < 60)
								tickNum++;
							else
								{
									tickNum = 0;
									letterNum++;
								}
							repaint();
							break;
					}
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
						for(int i=level.size()-1; i>=0; i--)
							{
								for(int j=0; j< level.get(i).size(); j++)
									{
										if(level.get(i).get(j)!=null)
											{
												if(level.get(i).get(j).getType().equals("spike"))
													{
														if(stage == 0)
															{
																if(level.get(i).get(j).getPos().getX() < letterNum * 40 * 5)
																	{
																		if(level.get(i).get(j).getCurrFrame()!=level.get(i).get(j).getMaxFrames()-1)
																			level.get(i).get(j).setCurrFrame(level.get(i).get(j).getCurrFrame()+1);
																	}
															}
														else
															{
																if(Math.sqrt(Math.pow(guy.getPos().getX()-level.get(i).get(j).getPos().getX(), 2)+Math.pow(guy.getPos().getY()-level.get(i).get(j).getPos().getY(), 2))<160)
																	
																	{
																		if(level.get(i).get(j).getCurrFrame()!=level.get(i).get(j).getMaxFrames()-1)
																			level.get(i).get(j).setCurrFrame(level.get(i).get(j).getCurrFrame()+1);
//																		else
//																			level.get(i).get(j).setCurrFrame(level.get(i).get(j).getMaxFrames()-1);
																	}
																else
																	{
																		if(level.get(i).get(j).getCurrFrame()!=0)
																			level.get(i).get(j).setCurrFrame(level.get(i).get(j).getCurrFrame()-1);
																	}
															}
													}
												if(level.get(i).get(j).getType().equals("lava") && level.get(i).get(j).getCurrFrame()<level.get(i).get(j).getMaxFrames())
													{
														level.get(i).get(j).setCurrFrame(level.get(i).get(j).getCurrFrame()+1);
														if(level.get(i).get(j).getCurrFrame()==level.get(i).get(j).getMaxFrames())
															level.get(i).get(j).setCurrFrame(0);
													}
											}
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
					if(stage == 4)
						{
							if(colorNum < colors.length - 1)
								{
									colorNum++;
								}
							else
								colorNum = 1;
						}
					}
			});
			animTimer.start();
		}
		
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Font z1 = new Font("Arial", Font.BOLD, 75);
			Font z2 = new Font("Arial", Font.PLAIN, 40);
			Font z3 = new Font("ARIAL", Font.BOLD, 100);
			Font z4 = new Font("ARIAL", Font.PLAIN, 50);
			Font z5 = new Font("ARIAL", Font.PLAIN, 30);
			switch(stage)
			{
				case 0:
					g.drawImage(skybox1.getImage(), skybox1.getPos().getX(), skybox1.getPos().getY(), null);
					g.drawImage(skybox2.getImage(), skybox2.getPos().getX(), skybox2.getPos().getY(), null);
					for(int r = level.size() - 1; r >= 0; r--)
						{
							for(int c = 0; c < level.get(r).size(); c++)
								{
									if(level.get(r).get(c) != null)
										{
										    if(level.get(r).get(c).getType().equals("spike"))
										    	{
										    		Block e = level.get(r).get(c);
										    		g.drawImage(e.getAnim().get(e.getCurrFrame()), e.getPos().getX(), e.getPos().getY(), e.getPos().getX() + 40, e.getPos().getY() + 40, 0, 0, 40, 40, null, null);
										    	}
										    else if(level.get(r).get(c).getType().equals("lava"))
										    	{
										    		Block e = level.get(r).get(c);
										    		g.drawImage(e.getAnim().get(e.getCurrFrame()), e.getPos().getX(), e.getPos().getY(), e.getPos().getX() + 40, e.getPos().getY() + 40, 0, 0, 40, 40, null, null);
										    	}
										    else if(!level.get(r).get(c).getType().equals(""))
												{
													g.drawImage(level.get(r).get(c).getImage(), level.get(r).get(c).getPos().getX(), level.get(r).get(c).getPos().getY(), null);	
												}
											else
												{	
//													g.setColor(level.get(r).get(c).getColor());
//													g.fillRect(level.get(r).get(c).getPos().getX(), level.get(r).get(c).getPos().getY(), size, size);
												}	
//											g.setColor(level.get(r).get(c).getColor());
//											g.fillRect(level.get(r).get(c).getPos().getX(), level.get(r).get(c).getPos().getY(), size, size);
										}
								}
						}
					if(letterNum >= 6)
						{
							Font s = new Font("Arial", Font.BOLD, 50);
							g.setFont(s);
							g.setColor(Color.BLACK);
							g.drawString("PRESS ENTER TO START", 210, 550);
						}				
					g.drawImage(guy.getAnim().get(guy.getCurrFrame()), guy.getPos().getX(), guy.getPos().getY(), guy.getPos().getX() + 40, guy.getPos().getY() + 40, 0, 0, 40, 40, null, null);
					for(Enemy e: goombas)
						{
							if(e.getVel().getX() > 0)
								g.drawImage(e.getAnim().get(e.getCurrFrame()), e.getPos().getX(), e.getPos().getY(), e.getPos().getX() + 40, e.getPos().getY() + 40, 0, 0, 40, 40, null, null);
							else if(e.getVel().getX() < 0)
								g.drawImage(e.getAnim().get(e.getCurrFrame()), e.getPos().getX(), e.getPos().getY(), e.getPos().getX() + 40, e.getPos().getY() + 40, 40, 0, 0, 40, null, null);
						}

					break;
				case 1:
					g.drawImage(skybox1.getImage(), skybox1.getPos().getX(), skybox1.getPos().getY(), null);
					g.drawImage(skybox2.getImage(), skybox2.getPos().getX(), skybox2.getPos().getY(), null);
					for(int r = level.size() - 1; r >= 0; r--)
						{
							for(int c = 0; c < level.get(r).size(); c++)
								{
									if(level.get(r).get(c) != null)
										{
										    if(level.get(r).get(c).getType().equals("spike"))
										    	{
										    		Block e = level.get(r).get(c);
										    		g.drawImage(e.getAnim().get(e.getCurrFrame()), e.getPos().getX(), e.getPos().getY(), e.getPos().getX() + 40, e.getPos().getY() + 40, 0, 0, 40, 40, null, null);
										    	}
										    else if(level.get(r).get(c).getType().equals("lava"))
										    	{
										    		Block e = level.get(r).get(c);
										    		g.drawImage(e.getAnim().get(e.getCurrFrame()), e.getPos().getX(), e.getPos().getY(), e.getPos().getX() + 40, e.getPos().getY() + 40, 0, 0, 40, 40, null, null);
										    	}
										    else if(!level.get(r).get(c).getType().equals(""))
												{
													g.drawImage(level.get(r).get(c).getImage(), level.get(r).get(c).getPos().getX(), level.get(r).get(c).getPos().getY(), null);	
												}
											else
												{	
//													g.setColor(level.get(r).get(c).getColor());
//													g.fillRect(level.get(r).get(c).getPos().getX(), level.get(r).get(c).getPos().getY(), size, size);
												}	
//											g.setColor(level.get(r).get(c).getColor());
//											g.fillRect(level.get(r).get(c).getPos().getX(), level.get(r).get(c).getPos().getY(), size, size);
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
					if(guy.isInvinc() && (guy.getInvFrames() % 2 == 0))
						{
							//Don't draw the guy
						}
					else
						{
							if(guy.getVel().getX() > 0)
								g.drawImage(guy.getAnim().get(guy.getCurrFrame()), guy.getPos().getX(), guy.getPos().getY(), guy.getPos().getX() + 40, guy.getPos().getY() + 40, 0, 0, 40, 40, null, null);
							else if(guy.getVel().getX() < 0)
								g.drawImage(guy.getAnim().get(guy.getCurrFrame()), guy.getPos().getX(), guy.getPos().getY(), guy.getPos().getX() + 40, guy.getPos().getY() + 40, 40, 0, 0, 40, null, null);
							else if(guy.getVel().getX() == 0 && lastDir.equals("r"))
								g.drawImage(guy.getAnim().get(guy.getCurrFrame()), guy.getPos().getX(), guy.getPos().getY(), guy.getPos().getX() + 40, guy.getPos().getY() + 40, 0, 0, 40, 40, null, null);
							else if(guy.getVel().getX() == 0 && lastDir.equals("l"))
								g.drawImage(guy.getAnim().get(guy.getCurrFrame()), guy.getPos().getX(), guy.getPos().getY(), guy.getPos().getX() + 40, guy.getPos().getY() + 40, 40, 0, 0, 40, null, null);
						}

					for(int i = 0; i < guy.getLives(); i++)
						{
							g.drawImage(guyLives.getImage(), guyLives.getPos().getX() + (45 * i), guyLives.getPos().getY(), null);
						}
					for(int i = 0; i < maxLives - guy.getLives(); i++)
						{
							g.drawImage(lostLives.getImage(), (45 * guy.getLives()) + (45 * i) + lostLives.getPos().getX(), lostLives.getPos().getY(), null);
						}
					
					g.drawImage(score.getImage(), score.getPos().getX(), score.getPos().getY(), null);
					Font f = new Font("Arial", Font.PLAIN, 30);
					g.setFont(f);
					g.drawString(scoreCounter + " / " + scoreTotal, 910, 40);
					
					

					for(Boss b: bosses)
						for(int i = 0; i < b.getHearts().size(); i++)
							g.drawImage(b.getHearts().get(i).getImage(), b.getHearts().get(i).getPos().getX(), b.getHearts().get(i).getPos().getY(), null);

					break;
				case 2:
					g.drawImage(skybox1.getImage(), skybox1.getPos().getX(), skybox1.getPos().getY(), null);
					g.drawImage(skybox2.getImage(), skybox2.getPos().getX(), skybox2.getPos().getY(), null);
					g.setFont(z1);
					g.drawString("LEVEL "+levelNum+" COMPLETE!", 130, 250);
					
					String scoreString = "";
					if(letterNum > 0)
						scoreString += "YOU GOT";
					if(letterNum > 1)
						scoreString += " "+scoreCounter;
					if(letterNum > 2)
						scoreString += " /";
					if(letterNum > 3)
						scoreString += " "+scoreTotal;
					if(letterNum > 4)
						g.drawImage(score.getImage(), 300 + (scoreString.length() * 21), 365, null);
					g.setFont(z2);
					g.drawString(scoreString, 300, 400);
					break;
				case 3:
					
					g.drawImage(skybox1.getImage(), skybox1.getPos().getX(), skybox1.getPos().getY(), null);
					g.drawImage(skybox2.getImage(), skybox2.getPos().getX(), skybox2.getPos().getY(), null);
					g.setFont(z3);
					g.setColor(Color.BLACK);
					g.drawString("YOU DIED", 260, 300);
					if(letterNum > 1)
						{
							g.setFont(z2);
							g.drawString("PRESS ENTER TO RETRY...", 260, 500);
						}
					break;
				case 4:
					g.setColor(colors[colorNum]);
					g.setFont(z3);
					g.drawString("YOU WIN!", 250, 200);
					g.setColor(Color.WHITE);
					g.setFont(z4);
					if(letterNum > 0)
						{
							g.drawString("Thank you for playing the Sword Demo!", 50, 300);
						}
					g.setFont(z4);
					if(letterNum > 1)
						g.drawString("Credits:", 400, 400);
					g.setFont(z5);
					if(letterNum > 2)
						{
							g.drawString("Lead Programmer: Andrew Shine", 250, 450);
							g.drawString("Lead Programmer: Josh Obrecht", 250, 490);
						}
					if(letterNum > 3)
						{
							g.drawString("Lead Artist: Andrew Shine", 300, 530);
							g.drawString("Lead Artist: Josh Obrecht", 300, 570);
						}
					if(letterNum > 4)
						{
							g.drawString("Lead Animator: Andrew Shine", 275, 610);
							g.drawString("Lead Animator: Josh Obrecht", 275, 650);
						}
					if(letterNum > 5)
						{
							g.drawString("Chief Credits Writer: Andrew Shine", 250, 690);
							g.drawString("Professional Rubber Duck: Josh Obrecht", 225, 730);
						}
					if(letterNum > 6)
						g.drawString("Fred Flintstone: Mr. Mike McGuire", 250, 800);
				}
			}

	     

		public void readLevel()
		{
			if(levelNum > 0 && levelNum < 4)
				{
//					SoundEffect.WARD.end();
//					SoundEffect.MARIO.loop();
				}
			else
				{
//					SoundEffect.MARIO.end();
//					SoundEffect.WARD.loop();
				}
			level = new ArrayList<ArrayList<Block>>();
			scoreTotal = 0;
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
								case 'k':
									goombas.add(new Enemy(new Vector(x,y), "skyenemy"));
									break;
								case 'q':
									goombas.add(new Enemy(new Vector(x,y), "lavaenemy"));
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
								case 's':
									b = new Block(new Vector(x,y), null, "spike");
									newLine.add(b);
									break;
								case '$':
									b = new Block(new Vector(x,y), null, "coin");
									scoreTotal++;
									newLine.add(b);
									break;
								case ' ':
									newLine.add(null);
									break;
								case 't':
									b = new Block(new Vector(x,y), Color.GRAY, "stone");
									newLine.add(b);
									break;
								case 'l':
									b = new Block(new Vector(x,y), null, "lava");
									newLine.add(b);
									break;
							}
							x += size;
						}
					level.add(newLine);
					x = size * -1;
					y += size;
				}
			if(levelNum < 4)
				{
					skybox1 = new Entity(new Vector(0,0), "sky1");
					skybox2 = new Entity(new Vector(1000,0), "sky2");
				}
			else
				{
					skybox1 = new Entity(new Vector(0,0), "hellsky1");
					skybox2 = new Entity(new Vector(1000,0), "hellsky2");
				}
			
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
			if(guy.isInvinc())
				{
					guy.setInvFrames(guy.getInvFrames() + 1);
					if(guy.getInvFrames() > guy.getMaxInvinc())
						{
							guy.setInvinc(false);
							guy.setInvFrames(0);
						}
				}
			boolean[] checks;
			for(int i = 0; i < Math.abs(guy.getVel().getX()); i++)
				{
					checkAllEnemies();
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
        
					if(checks[5])
						getHurt(lastDir);
				}
			for(int i = 0; i < Math.abs(guy.getPushVel().getX()); i++)
				{
					checkAllEnemies();
					checks = guy.checkEverything();
					if(!checks[1])
						{
							int increment = guy.getPushVel().getX() / Math.abs(guy.getPushVel().getX());
							if(checks[4])
								tick(increment, 0);
							else
								guy.getPos().setX(guy.getPos().getX() + increment);
						}

					guy.getLeftB().setLocation(guy.getPos().getX() - 1, guy.getPos().getY());
					guy.getRightB().setLocation(guy.getPos().getX() + size, guy.getPos().getY());
					guy.getHitBoxes()[4].setLocation(guy.getPos().getX(), guy.getPos().getY());
					if(checks[5])
						getHurt(lastDir);
				}
			if(guy.getPushVel().getX() > 0)
				{
					guy.getPushVel().setX(guy.getPushVel().getX() - 1);
				}
			else if(guy.getPushVel().getX() < 0)
				{
					guy.getPushVel().setX(guy.getPushVel().getX() + 1);
				}

			
			for(int i = 0; i < Math.abs(guy.getVel().getY()); i++)
				{
					checkAllEnemies();
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
					if(checks[5])
						getHurt(lastDir);
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
					stage = 2;
					tickNum = 0;
					letterNum = 0;
				}
			if(checks[5])
				getHurt(lastDir);
		}
		public void enemyTick()
		{
			for(Enemy e: goombas)
				{
					for(int i = 0; i < Math.abs(e.getVel().getX()); i++)
						{
							checkAllEnemies();
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
		}
		public void bossTick()
		{
			for(Boss b: bosses)
				{
					if(b.isInvinc())
						{
							b.setInvFrames(b.getInvFrames() + 1);
							if(b.getInvFrames() > b.getMaxInvinc())
								{
									b.setInvinc(false);
									b.setInvFrames(0);
								}
						}
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
							checkAllEnemies();
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
							checkAllEnemies();
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
					if(b.isInvinc())
						b.setCurrFrame(2);
				}
			bosses.removeAll(gc);
		}
		public void checkAllEnemies()
		{
			for(Enemy e: goombas)
				{
					if(Math.abs(guy.getPos().getX() - e.getPos().getX()) <= 40)
						{
							String collideCheck = checkEnemyCollide(e);
							if(collideCheck.substring(0,5).equals("death"))
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
						}
				}
			for(Boss b: bosses)
				{
					if(Math.abs(guy.getPos().getX() - b.getPos().getX()) <= 80)
						{
							String collideCheck = checkEnemyCollide(b);
							if(collideCheck.equals("bounce"))
								{
									guy.getVel().setY(-15);
									if(!b.isInvinc())
										{
											if(b.getHearts().size() > 0)
												{
													b.getHearts().remove(b.getHearts().size() - 1);
													b.setInvinc(true);
												}
											else
												{
													gc.add(b);
													stage = 4;
												}		
										}
									break;
								}
							else if(collideCheck.substring(0,5).equals("death"))
								{
									getHurt(collideCheck.substring(5));
									break;
								}
						}
					
				}
		}
		public String checkEnemyCollide(Enemy e)
		{
			for(Rectangle r: e.getHitBoxes())
				for(Rectangle h: guy.getHitBoxes())
					{
						if(r.intersects(h) && !h.equals(guy.getDownB()) && h.equals(guy.getRightB()) && !guy.isInvinc())
							return "deathl";
						else if(r.intersects(h) && !h.equals(guy.getDownB()) && h.equals(guy.getLeftB()) && !guy.isInvinc())
							return "deathr";
						else if(r.intersects(h) && h.equals(guy.getDownB()))
							return "bounce";
					}
					
			return "nooooooo";
		}
		public void getHurt(String dir)
		{
			if(!guy.isInvinc())
				guy.setLives(guy.getLives() - 1);
			if(guy.getLives() == 0)
				{
					stage = 3;
					letterNum = 0;
					tickNum = 0;
					return;
				}
			guy.setInvinc(true);
			guy.getVel().setY(-10);
			if(dir.equals("l"))
				guy.getPushVel().setX(-12);
			else if(dir.equals("r"))
				guy.getPushVel().setX(12);
		}
	}