// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add contract section to EXTCDS
// Transaction AddContrSection
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

// Date         Changed By                         Description
// 2023-04-10   Jessica Bjorklund (Columbus)       Creation
// 2024-08-09   Jessica Bjorklund (Columbus)       Change length of CSNA from 30 to 50


/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: CSSN - Section Number
 * @param: CSNA - Section Name
 * @param: DPOR - Display Order
 * @param: CSSP - Species
 * @param: CSGR - Grades
 * @param: CSEX - Exception Codes
 * @param: CSLI - Length Increment
 * @param: FRSC - Frequency Load
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DSID - Section ID
 * 
**/


public class AddContrSection extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inDSID
  boolean numberFound
  Integer lastNumber

  
  // Constructor 
  public AddContrSection(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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
           
     // Section Number
     int inCSSN 
     if (mi.in.get("CSSN") != null) {
        inCSSN = mi.in.get("CSSN") 
     } else {
        inCSSN = 0        
     }

     // Section Name
     String inCSNA  
     if (mi.in.get("CSNA") != null && mi.in.get("CSNA") != "") {
        inCSNA = mi.inData.get("CSNA").trim() 
     } else {
        inCSNA = ""      
     }

     // Display Order
     int inDPOR  
     if (mi.in.get("DPOR") != null) {
        inDPOR = mi.in.get("DPOR") 
     } else {
        inDPOR = 0        
     }

     // Species
     int inCSSP  
     if (mi.in.get("CSSP") != null) {
        inCSSP = mi.in.get("CSSP") 
     } else {
        inCSSP = 0        
     }
     
     // Grades
     int inCSGR  
     if (mi.in.get("CSGR") != null) {
        inCSGR = mi.in.get("CSGR") 
     } else {
        inCSGR = 0        
     }
     
     // Exception Codes
     int inCSEX 
     if (mi.in.get("CSEX") != null) {
        inCSEX = mi.in.get("CSEX") 
     } else {
        inCSEX = 0        
     }

     // Length Increment
     double inCSLI 
     if (mi.in.get("CSLI") != null) {
        inCSLI = mi.in.get("CSLI") 
     } else {
        inCSLI = 0d        
     }

     // Frequency Scaling
     int inFRSC 
     if (mi.in.get("FRSC") != null) {
        inFRSC = mi.in.get("FRSC") 
     } else {
        inFRSC = 0        
     }


     // Validate Contract Status record
     Optional<DBContainer> EXTCDS = findEXTCDS(inCONO, inDIVI, inRVID, inCSSN)
     if(EXTCDS.isPresent()){
        mi.error("Contract Section already exists")   
        return             
     } else {
        findLastNumber()
        inDSID = lastNumber + 1
        // Write record 
        addEXTCDSRecord(inCONO, inDIVI, inRVID, inDSID, inCSSN, inCSNA, inDPOR, inCSSP, inCSGR, inCSEX, inCSLI, inFRSC)          
     }  

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("DSID", String.valueOf(inDSID))      
     mi.write()

  }
  
    
   //******************************************************************** 
   // Find last id number used
   //********************************************************************  
   void findLastNumber(){   
     
     numberFound = false
     lastNumber = 0

     ExpressionFactory expression = database.getExpressionFactory("EXTCDS")
     
     expression = expression.eq("EXCONO", String.valueOf(inCONO)).and(expression.eq("EXDIVI", inDIVI))
     
     // Get Last Number
     DBAction actionline = database.table("EXTCDS")
     .index("20")
     .matching(expression)
     .selection("EXDSID")
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
        lastNumber = line.get("EXDSID") 
        numberFound = true
      }
  
  }

  //******************************************************************** 
  // Get EXTCDS record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCDS(int CONO, String DIVI, String RVID, int CSSN){  
     DBAction query = database.table("EXTCDS").index("00").build()
     DBContainer EXTCDS = query.getContainer()
     EXTCDS.set("EXCONO", CONO)
     EXTCDS.set("EXDIVI", DIVI)
     EXTCDS.set("EXRVID", RVID)
     EXTCDS.set("EXCSSN", CSSN)
     if(query.read(EXTCDS))  { 
       return Optional.of(EXTCDS)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTCDS record 
  //********************************************************************     
  void addEXTCDSRecord(int CONO, String DIVI, String RVID, int DSID, int CSSN, String CSNA, int DPOR, int CSSP, int CSGR, int CSEX, double CSLI, int FRSC){     
       DBAction action = database.table("EXTCDS").index("00").build()
       DBContainer EXTCDS = action.createContainer()
       EXTCDS.set("EXCONO", CONO)
       EXTCDS.set("EXDIVI", DIVI)
       EXTCDS.set("EXRVID", RVID)
       EXTCDS.set("EXDSID", DSID)
       EXTCDS.set("EXCSSN", CSSN)
       EXTCDS.set("EXCSNA", CSNA)
       EXTCDS.set("EXDPOR", DPOR)
       EXTCDS.set("EXCSSP", CSSP)
       EXTCDS.set("EXCSGR", CSGR)
       EXTCDS.set("EXCSEX", CSEX)
       EXTCDS.set("EXCSLI", CSLI)
       EXTCDS.set("EXFRSC", FRSC)   
       EXTCDS.set("EXCHID", program.getUser())
       EXTCDS.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTCDS.set("EXRGDT", regdate) 
       EXTCDS.set("EXLMDT", regdate) 
       EXTCDS.set("EXRGTM", regtime)
       action.insert(EXTCDS)         
 } 

     
} 

