# CommandsAPI
How to use:
1. Implement CommandListener in the wanted class.
2. Register that class from onEnable method or something
3. Put the @Command Annotation before the method
4. The method will have 2 paramters, CommandSender sender, and String[] args.

Example: The command is **pex group Owner user add erezbiox1**. the permission is **pex.admin**

    public class Main extends JavaPlugin implements CommandListener {
	    @Override
	    public void onEnable(){
		    CommandManager.register(this);
	    }
	    
	    @Command(permission = "pex.admin", wildcards = "group * user add *")
	    public void Pex(CommandSender sender, String[] args){
		    sender.sendMessage("Hello!");
	    }
	}

# Paramaters

name - the name will be the method name by defualt, this can be changed by using @Command(name="CommandName").

permission - the required permission. Op can override

wildcards - will be explained in the Wildcards section.

player - if on the sender must be a player, if it is on the method will need a Player paramter instead of a CommandSender

# Wildcards

The wildcards system is pretty cool, it's allowing you to use complex commands with ease.
the wildcards string is the required arguments for the command, but, every string that is a "*" will be a wildcard, 
meaning he can be anything. Because all of the other arguments are static, we're only giveing you the arguments for the wildcards.

Meaning if the wildcards is the following:
  @Command(name = "pex", wildcards = "group * user add *")
and the user enters
  pex group Owner user add Erezbiox1
The method will give you only the "Owner" and the "Erezbiox1".


# Maven
[See me on Jitpack!](https://jitpack.io/#Erezbiox1/CommandsAPI)
