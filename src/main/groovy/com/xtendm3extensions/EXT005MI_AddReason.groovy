// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add reason to EXTIRP
// Transaction AddReason
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RPNA - Reason Name
 * @param: RECD - Reason Code
 * @param: ISPC - Is Percentage
 * @param: LOAD - Loads
 * @param: DLOG - Logs
 * @param: GVBF - Gross Volume
 * @param: NVBF - Net Volume
 * @param: PCTG - Percentage
 * @param: EINV - Effect on Inventory
 * @param: NOTE - Note
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: RPID - Reason ID
 * 
**/


public class AddReason extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inRPID
  boolean numberFound
  Integer lastNumber

  
  // Constructor 
  public AddReason(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.utility = utility
     this.logger = logger
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

     // Reason Name
     String inRPNA
     if (mi.in.get("RPNA") != null && mi.in.get("RPNA") != "") {
        inRPNA = mi.inData.get("RPNA").trim() 
     } else {
        inRPNA = ""         
     }
     
     // Reason Code
     String inRECD
     if (mi.in.get("RECD") != null && mi.in.get("RECD") != "") {
        inRECD = mi.inData.get("RECD").trim() 
        
        // Validate Reason Code
        Optional<DBContainer> CSYTAB = findCSYTAB(inCONO, inRECD, "")
        if (!CSYTAB.isPresent()) {
           mi.error("Reason Code doesn't exist")   
           return  
        } 

     } else {
        inRECD = ""         
     }
           
     // Is Percentage
     int inISPC  
     if (mi.in.get("ISPC") != null) {
        inISPC = mi.in.get("ISPC") 
     } else {
        inISPC= 0      
     }

     // Loads
     double inLOAD  
     if (mi.in.get("LOAD") != null) {
        inLOAD = mi.in.get("LOAD") 
     } else {
        inLOAD = 0d        
     }

     // Logs
     double inDLOG  
     if (mi.in.get("DLOG") != null) {
        inDLOG = mi.in.get("DLOG") 
     } else {
        inDLOG = 0d        
     }
     
     // Gross Volume
     double inGVBF 
     if (mi.in.get("GVBF") != null) {
        inGVBF = mi.in.get("GVBF") 
     } else {
        inGVBF = 0d        
     }

     // Net Volume
     double inNVBF 
     if (mi.in.get("NVBF") != null) {
        inNVBF = mi.in.get("NVBF") 
     } else {
        inNVBF = 0d        
     }

     // Percentage
     double inPCTG 
     if (mi.in.get("PCTG") != null) {
        inPCTG = mi.in.get("PCTG") 
     } else {
        inPCTG = 0d        
     }

     // Effect on Inventory
     int inEINV  
     if (mi.in.get("EINV") != null) {
        inEINV = mi.in.get("EINV") 
     } else {
        inEINV= 0      
     }

     // Note
     String inNOTE
     if (mi.in.get("NOTE") != null && mi.in.get("NOTE") != "") {
        inNOTE = mi.inData.get("NOTE").trim() 
     } else {
        inNOTE = ""         
     }

     // Get next reason number
     findLastNumber()
     inRPID = lastNumber + 1

     // Validate Reason record
     Optional<DBContainer> EXTIRP = findEXTIRP(inCONO, inDIVI, inRPID)
     if(EXTIRP.isPresent()){
        mi.error("Deck Profile already exists")   
        return             
     } else {
        // Write record 
        addEXTIRPRecord(inCONO, inDIVI, inRPID, inRPNA, inRECD, inISPC, inLOAD, inDLOG, inGVBF, inNVBF, inPCTG, inEINV, inNOTE)          
     }  

     mi.write()

  }
  
  
   //******************************************************************** 
   // Find last id number used
   //********************************************************************  
   void findLastNumber(){   
     
     numberFound = false
     lastNumber = 0

     ExpressionFactory expression = database.getExpressionFactory("EXTIRP")
     
     expression = expression.eq("EXCONO", String.valueOf(inCONO)).and(expression.eq("EXDIVI", inDIVI))
     
     // Get Last Number
     DBAction actionline = database.table("EXTIRP")
     .index("00")
     .matching(expression)
     .selection("EXRPID")
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
        lastNumber = line.get("EXRPID") 
        numberFound = true
      }
  
  }


  //******************************************************************** 
  // Get EXTIRP record
  //******************************************************************** 
  private Optional<DBContainer> findEXTIRP(int CONO, String DIVI, int RPID){  
     DBAction query = database.table("EXTIRP").index("00").build()
     DBContainer EXTIRP = query.getContainer()
     EXTIRP.set("EXCONO", CONO)
     EXTIRP.set("EXDIVI", DIVI)
     EXTIRP.set("EXRPID", RPID)
     if(query.read(EXTIRP))  { 
       return Optional.of(EXTIRP)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Check RSCD in CSYTAB
  //******************************************************************** 
  private Optional<DBContainer> findCSYTAB(Integer CONO, String STKY, String LNCD){  
     DBAction query = database.table("CSYTAB").index("00").build()     
     DBContainer CSYTAB = query.getContainer()
     CSYTAB.set("CTCONO", CONO)
     CSYTAB.set("CTDIVI", "")
     CSYTAB.set("CTSTCO", "RSCD")
     CSYTAB.set("CTSTKY", STKY)
     CSYTAB.set("CTLNCD", LNCD)
    
     if(query.read(CSYTAB))  { 
       return Optional.of(CSYTAB)
     } 
  
     return Optional.empty()
  }

  
  //******************************************************************** 
  // Add EXTIRP record 
  //********************************************************************     
  void addEXTIRPRecord(int CONO, String DIVI, int RPID, String RPNA, String RECD, int ISPC, double LOAD, double DLOG, double GVBF, double NVBF, double PCTG, int EINV, String NOTE){     
       DBAction action = database.table("EXTIRP").index("00").build()
       DBContainer EXTIRP = action.createContainer()
       EXTIRP.set("EXCONO", CONO)
       EXTIRP.set("EXDIVI", DIVI)
       EXTIRP.set("EXRPID", RPID)
       EXTIRP.set("EXRPNA", RPNA)
       EXTIRP.set("EXRECD", RECD)
       EXTIRP.set("EXISPC", ISPC)
       EXTIRP.set("EXLOAD", LOAD)
       EXTIRP.set("EXDLOG", DLOG)
       EXTIRP.set("EXGVBF", GVBF)
       EXTIRP.set("EXNVBF", NVBF)
       EXTIRP.set("EXPCTG", PCTG)
       EXTIRP.set("EXEINV", EINV)
       EXTIRP.set("EXNOTE", NOTE)   
       EXTIRP.set("EXCHID", program.getUser())
       EXTIRP.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTIRP.set("EXRGDT", regdate) 
       EXTIRP.set("EXLMDT", regdate) 
       EXTIRP.set("EXRGTM", regtime)
       action.insert(EXTIRP)         
 } 

     
} 

