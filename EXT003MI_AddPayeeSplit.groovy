// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-07-06
// @version   1.0 
//
// Description 
// This API is to add payee split to EXTDPS
// Transaction AddPayeeSplit
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: STID - Scale Ticket ID
 * @param: ITNO - Item Number
 * @param: SEQN - Sequence
 * @param: CASN - Payee Number
 * @param: SUNM - Payee Name
 * @param: CF15 - Payee Role
 * @param: SUCM - Cost Element
 * @param: INBN - Invoice Batch Number
 * @param: CAAM - Charge Amount
 * 
*/


public class AddPayeeSplit extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  
  // Constructor 
  public AddPayeeSplit(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.logger = logger
     this.utility = utility
  } 
    
  public void main() {       
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Delivery Number
     int inDLNO  
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0        
     }

     // Scale Ticket ID
     int inSTID  
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0        
     }

     // Item Number
     String inITNO
     if (mi.in.get("ITNO") != null) {
        inITNO = mi.in.get("ITNO") 
     } else {
        inITNO = ""         
     }

     // Sequence
     int inSEQN  
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0        
     }

     // Payee Number
     String inCASN
     if (mi.in.get("CASN") != null) {
        inCASN = mi.in.get("CASN") 
     } else {
        inCASN = ""         
     }

     // Payee Name
     String inSUNM
     if (mi.in.get("SUNM") != null) {
        inSUNM = mi.in.get("SUNM") 
     } else {
        inSUNM = ""         
     }

     // Payee Role
     int inCF15  
     if (mi.in.get("CF15") != null) {
        inCF15 = mi.in.get("CF15") 
     } else {
        inCF15 = 0        
     }

     // Cost Element
     String inSUCM
     if (mi.in.get("SUCM") != null) {
        inSUCM = mi.in.get("SUCM") 
     } else {
        inSUCM = ""         
     }

     // Invoice Batch Number
     int inINBN  
     if (mi.in.get("INBN") != null) {
        inINBN = mi.in.get("INBN") 
     } else {
        inINBN = 0        
     }

     // Charge Amount
     double inCAAM  
     if (mi.in.get("CAAM") != null) {
        inCAAM = mi.in.get("CAAM") 
     } else {
        inCAAM = 0d        
     }
     

     // Validate Payee Split record
     Optional<DBContainer> EXTDPS = findEXTDPS(inCONO, inDIVI, inDLNO, inSTID, inITNO, inSEQN)
     if(EXTDPS.isPresent()){
        mi.error("Payee Split already exists")   
        return             
     } else {
        // Write record 
        addEXTDPSRecord(inCONO, inDIVI, inDLNO, inSTID, inITNO, inSEQN, inCASN, inSUNM, inCF15, inSUCM, inINBN, inCAAM)          
     }  

  }
  


  //******************************************************************** 
  // Get EXTDPS record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPS(int CONO, String DIVI, int DLNO, int STID, String ITNO, int SEQN){  
     DBAction query = database.table("EXTDPS").index("00").build()
     DBContainer EXTDPS = query.getContainer()
     EXTDPS.set("EXCONO", CONO)
     EXTDPS.set("EXDIVI", DIVI)
     EXTDPS.set("EXDLNO", DLNO)
     EXTDPS.set("EXSTID", STID)
     EXTDPS.set("EXITNO", ITNO)
     EXTDPS.set("EXSEQN", SEQN)
     if(query.read(EXTDPS))  { 
       return Optional.of(EXTDPS)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTDPS record 
  //********************************************************************     
  void addEXTDPSRecord(int CONO, String DIVI, int DLNO, int STID, String ITNO, int SEQN, String CASN, String SUNM, int CF15, String SUCM, int INBN, double CAAM){  
       DBAction action = database.table("EXTDPS").index("00").build()
       DBContainer EXTDPS = action.createContainer()
       EXTDPS.set("EXCONO", CONO)
       EXTDPS.set("EXDIVI", DIVI)
       EXTDPS.set("EXDLNO", DLNO)
       EXTDPS.set("EXSTID", STID)
       EXTDPS.set("EXITNO", ITNO)
       EXTDPS.set("EXSEQN", SEQN)
       EXTDPS.set("EXCASN", CASN)
       EXTDPS.set("EXSUNM", SUNM)
       EXTDPS.set("EXCF15", CF15)
       EXTDPS.set("EXSUCM", SUCM)
       EXTDPS.set("EXINBN", INBN)
       EXTDPS.set("EXCAAM", CAAM)   
       EXTDPS.set("EXCHID", program.getUser())
       EXTDPS.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTDPS.set("EXRGDT", regdate) 
       EXTDPS.set("EXLMDT", regdate) 
       EXTDPS.set("EXRGTM", regtime)
       action.insert(EXTDPS)         
 } 

     
} 

