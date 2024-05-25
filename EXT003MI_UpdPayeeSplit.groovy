// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-07-06
// @version   1.0 
//
// Description 
// This API is to update payee split in EXTDPS
// Transaction UpdPayeeSplit
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division Number
 * @param: DSID - Section ID
 * @param: CRSQ - Rate Sequence
 * @param: CRML - Min Length
 * @param: CRXL - Max Length
 * @param: CRMD - Min Diameter
 * @param: CRXD - Max Diameter
 * @param: CRRA - Amount
 * @param: CRNO - Note
 * 
*/



public class UpdPayeeSplit extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inSTID
  String inITNO
  int inSEQN
  String inCASN
  String inSUNM
  int inCF15
  String inSUCM
  int inINBN 
  double inCAAM 

  
  // Constructor 
  public UpdPayeeSplit(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0        
     }

     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0        
     }

     // Item Number
     if (mi.in.get("ITNO") != null && mi.in.get("ITNO") != "") {
        inITNO = mi.inData.get("ITNO").trim() 
     } else {
        inITNO = ""         
     }

     // Sequence
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0        
     }

     // Payee Number
     if (mi.in.get("CASN") != null && mi.in.get("CASN") != "") {
        inCASN = mi.inData.get("CASN").trim() 
        
        // Validate payee if entered
        Optional<DBContainer> CIDMAS = findCIDMAS(inCONO, inCASN)
        if (!CIDMAS.isPresent()) {
           mi.error("Payee doesn't exist")   
           return             
        }

     } else {
        inCASN = ""         
     }

     // Payee Name
     if (mi.in.get("SUNM") != null && mi.in.get("SUNM") != "") {
        inSUNM = mi.inData.get("SUNM").trim() 
     } else {
        inSUNM = ""         
     }

     // Payee Role
     if (mi.in.get("CF15") != null) {
        inCF15 = mi.in.get("CF15") 
     } 

     // Cost Element
     if (mi.in.get("SUCM") != null && mi.in.get("SUCM") != "") {
        inSUCM = mi.inData.get("SUCM").trim() 
     } else {
        inSUCM = ""         
     }

     // Invoice Batch Number
     if (mi.in.get("INBN") != null) {
        inINBN = mi.in.get("INBN") 
     }

     // Charge Amount
     if (mi.in.get("CAAM") != null) {
        inCAAM = mi.in.get("CAAM") 
     } 
     

     // Validate Payee Split record
     Optional<DBContainer> EXTDPS = findEXTDPS(inCONO, inDIVI, inDLNO, inSTID, inITNO, inSEQN)
     if(!EXTDPS.isPresent()){
        mi.error("Payee Split already exists")   
        return             
     } else {
        // Write record
        updEXTDPSRecord()            
     }
     
  }
  
  //******************************************************************** 
  // Get EXTDPS record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPS(int CONO, String DIVI, int DLNO, int STID, String ITNO, int SEQN){  
     DBAction query = database.table("EXTDPS").index("00").build()
     def EXTDPS = query.getContainer()
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
   // Check Supplier
   //******************************************************************** 
   private Optional<DBContainer> findCIDMAS(int CONO, String SUNO){  
     DBAction query = database.table("CIDMAS").index("00").build()   
     DBContainer CIDMAS = query.getContainer()
     CIDMAS.set("IDCONO", CONO)
     CIDMAS.set("IDSUNO", SUNO)
    
     if(query.read(CIDMAS))  { 
       return Optional.of(CIDMAS)
     } 
  
     return Optional.empty()
   }


  //******************************************************************** 
  // Update EXTDPS record
  //********************************************************************    
  void updEXTDPSRecord(){      
     DBAction action = database.table("EXTDPS").index("00").build()
     DBContainer EXTDPS = action.getContainer()     
     EXTDPS.set("EXCONO", inCONO)     
     EXTDPS.set("EXDIVI", inDIVI)  
     EXTDPS.set("EXDLNO", inDLNO)
     EXTDPS.set("EXSTID", inSTID)
     EXTDPS.set("EXITNO", inITNO)
     EXTDPS.set("EXSEQN", inSEQN)

     // Read with lock
     action.readAllLock(EXTDPS, 6, updateCallBackEXTDPS)
     }
   
     Closure<?> updateCallBackEXTDPS = { LockedResult lockedResult -> 
       if (inCASN != null && inCASN != "") {
          lockedResult.set("EXCASN", inCASN)
       }
  
       if (inSUNM != null && inSUNM != "") {
          lockedResult.set("EXSUNM", inSUNM)
       }
  
       if (inCF15 != null) {
          lockedResult.set("EXCF15", inCF15)
       }
  
       if (inSUCM != null && inSUCM != "") {
          lockedResult.set("EXSUCM", inSUCM)
       }
  
       if (inINBN != null) {
          lockedResult.set("EXINBN", inINBN)
       }
       
       if (inCAAM != null) {  
          lockedResult.set("EXCAAM", inCAAM)
       }
        
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changedate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changedate)        
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }


} 

