package com.coldsteelstudios.irc;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;

public enum MessageCode {

	RPL_WELCOME
		( "001", MessageType.LOGIN ), 
	RPL_YOURHOST  
		( "002", MessageType.LOGIN ),
	RPL_CREATED 
		( "003", MessageType.LOGIN ),
	RPL_MYINFO 
		( "004", MessageType.LOGIN ),
	RPL_BOUNCE
		( "005", MessageType.LOGIN ),

	RPL_TRACELINK   ("200"),
	RPL_TRACECONNECTING   ("201"),
	RPL_TRACEHANDSHAKE   ("202"),
	RPL_TRACEUNKNOWN   ("203"),
	RPL_TRACEOPERATOR   ("204"),
	RPL_TRACEUSER   ("205"),
	RPL_TRACESERVER   ("206"),
	RPL_TRACESERVICE   ("207"),
	RPL_TRACENEWTYPE   ("208"),
	RPL_TRACECLASS   ("209"),
	RPL_TRACERECONNECT   ("210"),
	RPL_STATSLINKINFO   ("211"),
	RPL_STATSCOMMANDS   ("212"),
	RPL_ENDOFSTATS   ("219"),

	RPL_UMODEIS 
		( "221", MessageType.USERMODE ),

	//unreal
	RPL_STATSGLINE ( "223"),

	RPL_SERVLIST   ("234"),
	RPL_SERVLISTEND   ("235"),
	RPL_STATSUPTIME   ("242"),
	RPL_STATSOLINE   ("243"),

	RPL_LUSERCLIENT 
		( "251", MessageType.INFO ),
	RPL_LUSEROP 
		( "252", MessageType.INFO ),
	RPL_LUSERUNKNOWN
		( "253", MessageType.INFO ),
	RPL_LUSERCHANNELS
		( "254", MessageType.INFO ),
	RPL_LUSERME
		( "255", MessageType.INFO ),

	RPL_ADMINME   ("256"),
	RPL_ADMINLOC1   ("257"),
	RPL_ADMINLOC2   ("258"),
	RPL_ADMINEMAIL   ("259"),
	RPL_TRACELOG   ("261"),
	RPL_TRACEEND   ("262"),
	RPL_TRYAGAIN   ("263"),
	RPL_AWAY   ("301"),
	RPL_USERHOST   ("302"),
	RPL_ISON   ("303"),
	RPL_UNAWAY   ("305"),
	RPL_NOWAWAY   ("306"),

	RPL_WHOISREGISTERED 
		( "307", MessageType.WHOIS ),
	RPL_WHOISUSER
		( "311", MessageType.WHOIS ),
	RPL_WHOISSERVER 
		( "312", MessageType.WHOIS ),
	RPL_WHOISOPERATOR
		( "313", MessageType.WHOIS ),
	RPL_WHOISIDLE
		( "317", MessageType.WHOIS ),
	RPL_ENDOFWHOIS
		( "318", MessageType.WHOIS ),
	RPL_WHOISCHANNELS
		( "319", MessageType.WHOIS ),

	RPL_WHOREPLY 
		( "352", MessageType.WHO ),
	RPL_ENDOFWHO 
		( "315", MessageType.WHO ),


	RPL_LISTSTART 
		( "321", MessageType.NONRELEVANT ),

	RPL_LIST 
		( "322", MessageType.LIST ),
	RPL_LISTEND 
		( "323", MessageType.LIST ),

	RPL_CHANNELMODEIS
		( "324", MessageType.CHANNELMODE ),

	RPL_UNIQOPIS ("325"),
	RPL_NOTOPIC ("331"),

	RPL_TOPIC
		( "332", MessageType.TOPIC ),

	RPL_USERIP   ("340"),
	RPL_INVITING   ("341"),
	RPL_SUMMONING   ("342"),
	RPL_INVITELIST   ("346"),
	RPL_ENDOFINVITELIST   ("347"),
	RPL_EXCEPTLIST   ("348"),
	RPL_ENDOFEXCEPTLIST   ("349"),
	RPL_VERSION   ("351"),

	RPL_LINKS   ("364"),
	RPL_ENDOFLINKS   ("365"),

	RPL_ENDOFNAMES 
		( "366", MessageType.NAME ),
	RPL_NAMREPLY  
		( "353", MessageType.NAME ),

	RPL_BANLIST
		( "367", MessageType.BANLIST ),
	RPL_ENDOFBANLIST 
		( "368", MessageType.BANLIST ),

	RPL_ENDOFWHOWAS
		( "369", MessageType.WHOWAS ),
	RPL_WHOWASUSER
		( "314", MessageType.WHOWAS ),

	RPL_INFO  ("371"),
	RPL_ENDOFINFO ("374"),

	RPL_MOTDSTART 
		( "375", MessageType.MOTD ),
	RPL_MOTD 
		( "372", MessageType.MOTD ),
	RPL_ENDOFMOTD
		( "376", MessageType.MOTD ),
	
	RPL_YOUREOPER 
		("381"),
	RPL_REHASHING   ("382"),
	RPL_YOURESERVICE   ("383"),
	RPL_TIME   ("391"),
	RPL_USERSSTART   ("392"),
	RPL_USERS   ("393"),
	RPL_ENDOFUSERS   ("394"),
	RPL_NOUSERS   ("395"),


	//These are all handled in the constructor...
	ERR_NOSUCHNICK   ("401"),
	ERR_NOSUCHSERVER   ("402"),
	ERR_NOSUCHCHANNEL   ("403"),
	ERR_CANNOTSENDTOCHAN   ("404"),
	ERR_TOOMANYCHANNELS   ("405"),
	ERR_WASNOSUCHNICK   ("406"),
	ERR_TOOMANYTARGETS   ("407"),
	ERR_NOSUCHSERVICE   ("408"),
	ERR_NOORIGIN   ("409"),
	ERR_NORECIPIENT   ("411"),
	ERR_NOTEXTTOSEND   ("412"),
	ERR_NOTOPLEVEL   ("413"),
	ERR_WILDTOPLEVEL   ("414"),
	ERR_BADMASK   ("415"),
	ERR_UNKNOWNCOMMAND   ("421"),
	ERR_NOMOTD   ("422"),
	ERR_NOADMININFO   ("423"),
	ERR_FILEERROR   ("424"),
	ERR_NONICKNAMEGIVEN   ("431"),
	ERR_ERRONEUSNICKNAME   ("432"),
	ERR_NICKNAMEINUSE   ("433"),
	ERR_NICKCOLLISION   ("436"),
	ERR_UNAVAILRESOURCE   ("437"),
	ERR_USERNOTINCHANNEL   ("441"),
	ERR_NOTONCHANNEL   ("442"),
	ERR_USERONCHANNEL   ("443"),
	ERR_NOLOGIN   ("444"),
	ERR_SUMMONDISABLED   ("445"),
	ERR_USERSDISABLED   ("446"),
	ERR_NOTREGISTERED   ("451"),
	ERR_NEEDMOREPARAMS   ("461"),
	ERR_ALREADYREGISTRED   ("462"),
	ERR_NOPERMFORHOST   ("463"),
	ERR_PASSWDMISMATCH   ("464"),
	ERR_YOUREBANNEDCREEP   ("465"),
	ERR_YOUWILLBEBANNED   ("466"),
	ERR_KEYSET   ("467"),
	ERR_CHANNELISFULL   ("471"),
	ERR_UNKNOWNMODE   ("472"),
	ERR_INVITEONLYCHAN   ("473"),
	ERR_BANNEDFROMCHAN   ("474"),
	ERR_BADCHANNELKEY   ("475"),
	ERR_BADCHANMASK   ("476"),
	ERR_NOCHANMODES   ("477"),
	ERR_BANLISTFULL   ("478"),
	ERR_NOPRIVILEGES   ("481"),
	ERR_CHANOPRIVSNEEDED   ("482"),
	ERR_CANTKILLSERVER   ("483"),
	ERR_RESTRICTED   ("484"),
	ERR_UNIQOPPRIVSNEEDED   ("485"),
	ERR_NOOPERHOST   ("491"),
	ERR_UMODEUNKNOWNFLAG   ("501"),
	ERR_USERSDONTMATCH   ("502");


	private String code;
	private MessageType type;
	
	private static final Map<String,MessageCode> lookup = new HashMap<String,MessageCode>();

	static {
		for(MessageCode s : EnumSet.allOf(MessageCode.class))
			lookup.put(s.code, s);
	}

	MessageCode( String code ) {
		this.code = code;

		int c = Integer.parseInt(code);
		if ( c >= 400 && c < 600 )
			this.type = MessageType.ERROR;
		else
			this.type = MessageType.UNKNOWN;
	}

	MessageCode( String code , MessageType type ) {
		this.code = code;
		this.type = type;
	}

	public static MessageCode get(String cmd) {
		return lookup.get(cmd);
	}

	public String getCode() {
		return code;
	}

	public MessageType getType() {
		return type;	
	}
}
