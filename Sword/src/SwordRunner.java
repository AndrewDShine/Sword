import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class SwordRunner extends JPanel
	{
		public Entity guy = new Entity(new Vector(-48, -48));
		public ArrayList<ArrayList<Block>> level = new ArrayList<ArrayList<Block>>();
		public Vector levelVel = new Vector(0,0);
		public String xDir = "";
		public boolean isJumping = false;
		public final int size = 40;
		
		public static void main(String[] args)
			{
				JFrame frame = new JFrame("Sword");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(1000, 880);
				SwordRunner game = new SwordRunner();
				frame.add(game);
				frame.setVisible(true);
				frame.setResizable(false);
				game.setFocusable(true);
				
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
									xDir = "r";
									break;
								case KeyEvent.VK_LEFT:
									xDir = "l";
									break;
								case KeyEvent.VK_UP:
									isJumping = true;
									break;
							}
						}
						public void keyReleased(KeyEvent e)
						{
							switch(e.getKeyCode())
							{
								case KeyEvent.VK_RIGHT:
									xDir = "";
									break;
								case KeyEvent.VK_LEFT:
									xDir = "";
									break;
								case KeyEvent.VK_UP:
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
							checkStanding();
							if(guy.isStanding())
								guy.getVel().setY(-20);
						}
					playerTick();
					repaint();
				}
			});
			timer.start();
		}
		
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			for(int r = level.size() - 1; r >= 0; r--)
				{
					for(int c = 0; c < level.get(r).size(); c++)
						{
							if(level.get(r).get(c) != null)
								{
									g.setColor(level.get(r).get(c).getColor());
									g.fillRect(level.get(r).get(c).getPos().getX(), level.get(r).get(c).getPos().getY(), size, size);
								}
						}
				}
			g.setColor(Color.MAGENTA);
			g.fillRect((int)guy.getLeftB().getX(), (int)guy.getLeftB().getY(), 1, 40);
			g.fillRect((int)guy.getRightB().getX(), (int)guy.getRightB().getY(), 1, 40);
			g.fillRect((int)guy.getUpB().getX(), (int)guy.getUpB().getY(), 40, 1);
			g.fillRect((int)guy.getDownB().getX(), (int)guy.getDownB().getY(), 40, 1);
			g.setColor(Color.black);
			g.fillRect(guy.getPos().getX(), guy.getPos().getY(), size, size);
		}
		
		public void readLevel()
		{
			Scanner levelReader = null;
			int position = 0;
			try
				{
					levelReader = new Scanner(new File("level.txt"));
				} catch (FileNotFoundException e)
				{
//					System.out.println("Level not found");
					System.out.println("BET");
				}
			while(levelReader.hasNextLine())
				{
					ArrayList<Block> newLine = new ArrayList<Block>();
					String levelLine = levelReader.nextLine();
					char[] levelArray = levelLine.toCharArray();
					for(char c: levelArray)
						{
							switch(c)
							{
								case 'g':
									b = new Block(new Vector(0,0), Color.GREEN, null);
									newLine.add(b);
									b.loadInformation();
									break;
								case 'r':
									b = new Block(new Vector(0,0), Color.RED, null);
									newLine.add(b);
									b.loadInformation();
									break;
								case 'c':
									newLine.add(new Block(new Vector(0,0), Color.CYAN, null));
									break;
								case 'p':
									guy = new Entity(new Vector(44, 721));
									break;
								case ' ':
									newLine.add(null);
									break;
							}
						}
					level.add(newLine);
				}
			int x = 0;
			int y = 812;
			for(int r = level.size() - 1; r >= 0; r--)
				{
					for(int c = 0; c < level.get(r).size(); c++)
						{
							if(level.get(r).get(c) != null)
								level.get(r).get(c).setPos(new Vector(x, y));
							x += size;
						}
					x = 0;
					y -= size;
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
		}
		public void playerTick()
		{
			for(int i = 0; i < Math.abs(guy.getVel().getX()); i++)
				{
					if(!checkWall())
						{
							int increment = guy.getVel().getX() / Math.abs(guy.getVel().getX());
							if(checkLevelMove())
								tick(increment, 0);
							else
								guy.getPos().setX(guy.getPos().getX() + increment);
						}
					guy.getLeftB().setLocation(guy.getPos().getX() - 1, guy.getPos().getY());
					guy.getRightB().setLocation(guy.getPos().getX() + 40, guy.getPos().getY());
				}
			for(int i = 0; i < Math.abs(guy.getVel().getY()); i++)
				{
					checkStanding();
					if(!guy.isStanding() || guy.getVel().getY() < 0)
						{
							if(guy.getVel().getY() != 0)
								{
									int increment = guy.getVel().getY() / Math.abs(guy.getVel().getY());
									guy.getPos().setY(guy.getPos().getY() + increment);
								}
						}
					else if(guy.isStanding())
						{
							guy.getVel().setY(0);
							break;
						}
					guy.getUpB().setLocation(guy.getPos().getX(), guy.getPos().getY() - 1);
					guy.getDownB().setLocation(guy.getPos().getX(), guy.getPos().getY() + 40);
				}
			guy.getLeftB().setLocation(guy.getPos().getX() - 1, guy.getPos().getY());
			guy.getRightB().setLocation(guy.getPos().getX() + 40, guy.getPos().getY());
			guy.getUpB().setLocation(guy.getPos().getX(), guy.getPos().getY() - 1);
			guy.getDownB().setLocation(guy.getPos().getX(), guy.getPos().getY() + 40);
			checkStanding();
			if(!guy.isStanding() && guy.getVel().getY() < 15)
				{
					guy.getVel().setY(guy.getVel().getY() + 1);
				}
		}
		public void checkStanding()
		{
			guy.setStanding(false);
			for(ArrayList<Block> line: level)
				{
					for(Block b: line)
						{
							if(b != null)
								{
									b.tick();
									if(b.getBounds().intersects(guy.getDownB()))
										{
											guy.setStanding(true);
										}
//									else if(b is a double-jumpy block)
//										{
//											check upB as well;
//										}
									if(b.getBounds().intersects(guy.getUpB()))
										{
											guy.getVel().setY(0);
										}
								}								
						}
				}
		}
		public boolean checkLevelMove()
		{
			if((guy.getPos().getX() == 40 && guy.getVel().getX() < 0) || (guy.getPos().getX() == 460 && guy.getVel().getX() > 0))
				{
					return true;
				}
			return false;
		}
		public boolean checkWall()
		{
			boolean inWall = false;
			for(ArrayList<Block> line: level)
				{
					for(Block b: line)
						{
							if(b != null)
								{
									b.tick();
									if(b.getBounds().intersects(guy.getLeftB()) && guy.getVel().getX() < 0)
										{
											inWall = true;;
										}
									else if(b.getBounds().intersects(guy.getRightB()) && guy.getVel().getX() > 0)
										{
											inWall = true;;
										}
								}								
						}
				}
			return inWall;
		}
	}