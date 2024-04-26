// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add contract status to EXTCTS
// Transaction AddContrStatus
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: SEQN - Sequence
 * @param: STAT - Revision Status
 * @param: MDUL - Module
 * @param: CSDT - Date Updated
 * @param: USID - Updated By
 * @param: NOTE - Comment
 * 
*/

/**
 * @return:  CONO - Company Number
 * @return:  DIVI - Division
 * @return:  CSID - Status ID
**/


public class AddContrStatus extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inCSID
  boolean numberFound
  Integer lastNumber

  
  // Constructor 
  public AddContrStatus(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Revision ID
     String inRVID
     if (mi.in.get("RVID") != null && mi.in.get("RVID") != "") {
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""         
     }
           
     // Sequence
     int inSEQN 
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0        
     }

     // Status
     int inSTAT  
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.in.get("STAT") 
     } else {
        inSTAT = 0        
     }

     // Module
     String inMDUL  
     if (mi.in.get("MDUL") != null && mi.in.get("MDUL") != "") {
        inMDUL = mi.inData.get("MDUL").trim() 
     } else {
        inMDUL = ""      
     }

     // Date Updated
     int inCSDT  
     if (mi.in.get("CSDT") != null) {
        inCSDT = mi.in.get("CSDT") 
        
        //Validate date format
        boolean validCSDT = utility.call("DateUtil", "isDateValid", String.valueOf(inCSDT), "yyyyMMdd")  
        if (!validCSDT) {
           mi.error("Date is not valid")   
           return  
        } 

     } else {
        inCSDT = 0        
     }

     // Updated By
     String inUSID  
     if (mi.in.get("USID") != null && mi.in.get("USID") != "") {
        inUSID = mi.inData.get("USID").trim() 
        
        // Validate user if entered
        Optional<DBContainer> CMNUSR = findCMNUSR(inCONO, inUSID)
        if (!CMNUSR.isPresent()) {
           mi.error("User doesn't exist")   
           return             
        }

     } else {
        inUSID = ""        
     }

     // Note
     String inNOTE
     if (mi.in.get("NOTE") != null && mi.in.get("NOTE") != "") {
        inNOTE = mi.inData.get("NOTE").trim() 
     } else {
        inNOTE = ""        
     }

     // Validate Contract Status record
     Optional<DBContainer> EXTCTS = findEXTCTS(inCONO, inDIVI, inRVID, inSEQN)
     if(EXTCTS.isPresent()){
        mi.error("Contract Status already exists")   
        return             
     } else {
        findLastNumber()
        inCSID = lastNumber + 1
        // Write record 
        addEXTCTSRecord(inCONO, inDIVI, inRVID, inSEQN, inSTAT, inMDUL, inCSDT, inUSID, inNOTE, inCSID)          
     }  

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("CSID", String.valueOf(inCSID))      
     mi.write()
  }
  
    
   //******************************************************************** 
   // Find last id number used
   //********************************************************************  
   void findLastNumber(){   
     
     numberFound = false
     lastNumber = 0

     ExpressionFactory expression = database.getExpressionFactory("EXTCTS")
     
     expression = expression.eq("EXCONO", String.valueOf(inCONO)).and(expression.eq("EXDIVI", inDIVI))
     
     // Get Last Number
     DBAction actionline = database.table("EXTCTS")
     .index("10")
     .matching(expression)
     .selection("EXCSID")
     .reverse()
     .build()

     DBContainer line = actionline.getContainer() 
     
     line.set("EXCONO", inCONO)     
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)   
   
   } 

    
  //******************************************************************** 
  // List Last Number
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line -> 

      // Output
      if (!lastNumber) {
        lastNumber = line.get("EXCSID") 
        numberFound = true
      }
  
  }
  
  //******************************************************************** 
  // Get EXTCTS record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTS(int CONO, String DIVI, String RVID, int SEQN){  
     DBAction query = database.table("EXTCTS").index("00").build()
     DBContainer EXTCTS = query.getContainer()
     EXTCTS.set("EXCONO", CONO)
     EXTCTS.set("EXDIVI", DIVI)
     EXTCTS.set("EXRVID", RVID)
     EXTCTS.set("EXSEQN", SEQN)
     if(query.read(EXTCTS))  { 
       return Optional.of(EXTCTS)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Check User
  //******************************************************************** 
  private Optional<DBContainer> findCMNUSR(int CONO, String USID){  
     DBAction query = database.table("CMNUSR").index("00").build()   
     DBContainer CMNUSR = query.getContainer()
     CMNUSR.set("JUCONO", 0)
     CMNUSR.set("JUDIVI", "")
     CMNUSR.set("JUUSID", USID)
    
     if(query.read(CMNUSR))  { 
       return Optional.of(CMNUSR)
     } 
  
     return Optional.empty()
  }

  
  //******************************************************************** 
  // Add EXTCTS record 
  //********************************************************************     
  void addEXTCTSRecord(int CONO, String DIVI, String RVID, int SEQN, int STAT, String MDUL, int CSDT, String USID, String NOTE, int CSID){     
       DBAction action = database.table("EXTCTS").index("00").build()
       DBContainer EXTCTS = action.createContainer()
       EXTCTS.set("EXCONO", CONO)
       EXTCTS.set("EXDIVI", DIVI)
       EXTCTS.set("EXRVID", RVID)
       EXTCTS.set("EXCSID", CSID)
       EXTCTS.set("EXSEQN", SEQN)
       EXTCTS.set("EXSTAT", STAT)
       EXTCTS.set("EXMDUL", MDUL)
       EXTCTS.set("EXCSDT", CSDT)
       EXTCTS.set("EXUSID", USID)
       EXTCTS.set("EXNOTE", NOTE)   
       EXTCTS.set("EXCHID", program.getUser())
       EXTCTS.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTCTS.set("EXRGDT", regdate) 
       EXTCTS.set("EXLMDT", regdate) 
       EXTCTS.set("EXRGTM", regtime)
       action.insert(EXTCTS)         
 } 

     
} 

