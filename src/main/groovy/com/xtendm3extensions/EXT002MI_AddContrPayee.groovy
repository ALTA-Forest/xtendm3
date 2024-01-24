// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add a new contract payee to EXTCTP
// Transaction AddContrPayee
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: CASN - Payee Number
 * @param: PYNM - Payee Name
 * @param: CF15 - Payee Role
 * @param: SHTP - Share Type
 * @param: CATF - Take From ID
 * @param: TFNM - Take From Name
 * @param: CATP - Take Priority
 * @param: CAAM - Amount
 * @param: CASA - Test Share
 * @param: PLVL - Level
 * @param: SLVL - Sub-Level
 * @param: PPID - Parent Payee ID
 * @param: ISAH - Is Alternate Hauler
 * @param: TRCK - Truck
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: CPID - Payee ID
 * 
**/

public class AddContrPayee extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inCPID
  boolean numberFound
  Integer lastNumber

  
  // Constructor 
  public AddContrPayee(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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
     if (mi.in.get("RVID") != null) {
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""         
     }
      
     // Payee Number
     String inCASN
     if (mi.in.get("CASN") != null) {
        inCASN = mi.inData.get("CASN").trim() 
     } else {
        inCASN = ""         
     }
    
     // Payee Name
     String inPYNM
     if (mi.in.get("PYNM") != null) {
        inPYNM = mi.inData.get("PYNM").trim() 
     } else {
        inPYNM = ""         
     }
 
     // Payee Role
     int inCF15
     if (mi.in.get("CF15") != null) {
        inCF15 = mi.in.get("CF15") 
     } else {
        inCF15 = 0       
     }
     
     // Share Type
     String inSHTP
     if (mi.in.get("SHTP") != null) {
        inSHTP = mi.inData.get("SHTP").trim() 
     } else {
        inSHTP = ""         
     }
    
     // Take From ID
     String inCATF
     if (mi.in.get("CATF") != null) {
        inCATF = mi.inData.get("CATF").trim() 
     } else {
        inCATF = ""         
     }    

     // Take From Name
     String inTFNM
     if (mi.in.get("TFNM") != null) {
        inTFNM = mi.inData.get("TFNM").trim() 
     } else {
        inTFNM = ""         
     }
    
     // Take Priority
     int inCATP
     if (mi.in.get("CATP") != null) {
        inCATP = mi.in.get("CATP") 
     } else {
        inCATP = 0       
     }
    
     // Amount
     double inCAAM
     if (mi.in.get("CAAM") != null) {
        inCAAM = mi.in.get("CAAM") 
     } else {
        inCAAM = 0d       
     }

     // Test Share
     double inCASA
     if (mi.in.get("CASA") != null) {
        inCASA = mi.in.get("CASA") 
     } else {
        inCASA = 0d       
     }
     
     // Level
     int inPLVL
     if (mi.in.get("PLVL") != null) {
        inPLVL = mi.in.get("PLVL") 
     } else {
        inPLVL = 1       
     }

     // Sub-Level
     int inSLVL
     if (mi.in.get("SLVL") != null) {
        inSLVL = mi.in.get("SLVL") 
     } else {
        inSLVL = 1       
     }
     
     // Parent Payee ID
     int inPPID
     if (mi.in.get("PPID") != null) {
        inPPID = mi.in.get("PPID") 
     } else {
        inPPID = 0       
     }   
    
     // Is Alternate Hauler
     int inISAH
     if (mi.in.get("ISAH") != null) {
        inISAH = mi.in.get("ISAH") 
     } else {
        inISAH = 0       
     }   

     // Truck
     String inTRCK
     if (mi.in.get("TRCK") != null) {
        inTRCK = mi.inData.get("TRCK").trim() 
     } else {
        inTRCK = ""         
     } 
     
     findLastNumber()
     inCPID = lastNumber + 1
 
     // Validate contract payee record
     Optional<DBContainer> EXTCTP = findEXTCTP(inCONO, inDIVI, inRVID, inCPID)
     if(EXTCTP.isPresent()){
        mi.error("Contract Payee already exists")   
        return             
     } else {
        // Write record 
        addEXTCTPRecord(inCONO, inDIVI, inRVID, inCPID, inCASN, inPYNM, inCF15, inSHTP, inCATF, inTFNM, inCATP, inCAAM, inCASA, inPLVL, inSLVL, inPPID, inISAH, inTRCK)          
     }  

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("CPID", String.valueOf(inCPID))      
     mi.write()

  }
  

   //******************************************************************** 
   // Find last id number used
   //********************************************************************  
   void findLastNumber(){   
     
     numberFound = false
     lastNumber = 0

     ExpressionFactory expression = database.getExpressionFactory("EXTCTP")
     
     expression = expression.eq("EXCONO", String.valueOf(inCONO)).and(expression.eq("EXDIVI", inDIVI))
     
     // Get Last Number
     DBAction actionline = database.table("EXTCTP")
     .index("10")
     .matching(expression)
     .selection("EXCPID")
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
        lastNumber = line.get("EXCPID") 
        numberFound = true
      }
  
  }
   
  //******************************************************************** 
  // Get EXTCTP record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTP(int CONO, String DIVI, String RVID, int CPID){  
     DBAction query = database.table("EXTCTP").index("00").build()
     DBContainer EXTCTP = query.getContainer()
     EXTCTP.set("EXCONO", CONO)
     EXTCTP.set("EXDIVI", DIVI)
     EXTCTP.set("EXRVID", RVID)
     EXTCTP.set("EXCPID", CPID)
     if(query.read(EXTCTP))  { 
       return Optional.of(EXTCTP)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTCTP record 
  //********************************************************************     
  void addEXTCTPRecord(int CONO, String DIVI, String RVID, int CPID, String CASN, String SUNM, int CF15, String SHTP, String CATF, String TFNM, int CATP, double CAAM, double CASA, int PLVL, int SLVL, int PPID, int ISAH, String TRCK){     
       DBAction action = database.table("EXTCTP").index("00").build()
       DBContainer EXTCTP = action.createContainer()
       EXTCTP.set("EXCONO", CONO)
       EXTCTP.set("EXDIVI", DIVI)
       EXTCTP.set("EXRVID", RVID)
       EXTCTP.set("EXCPID", CPID)
       EXTCTP.set("EXCASN", CASN)
       EXTCTP.set("EXSUNM", SUNM)
       EXTCTP.set("EXCF15", CF15)
       EXTCTP.set("EXSHTP", SHTP)
       EXTCTP.set("EXCATF", CATF)
       EXTCTP.set("EXTFNM", TFNM)
       EXTCTP.set("EXCATP", CATP)
       EXTCTP.set("EXCAAM", CAAM)
       EXTCTP.set("EXCASA", CASA)
       EXTCTP.set("EXPLVL", PLVL)
       EXTCTP.set("EXSLVL", SLVL)
       EXTCTP.set("EXPPID", PPID)
       EXTCTP.set("EXISAH", ISAH)
       EXTCTP.set("EXTRCK", TRCK)   
       EXTCTP.set("EXCHID", program.getUser())
       EXTCTP.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTCTP.set("EXRGDT", regdate) 
       EXTCTP.set("EXLMDT", regdate) 
       EXTCTP.set("EXRGTM", regtime)
       action.insert(EXTCTP)         
 } 
     
} 

