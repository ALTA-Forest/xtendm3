// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add contract section species to EXTCSS
// Transaction AddSectionSpec
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DSID - Section ID
 * @param: SPEC - Species
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: SSID - Species ID
*/


public class AddSectionSpec extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inSSID
  boolean numberFound
  Integer lastNumber

  // Constructor 
  public AddSectionSpec(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Section ID
     int inDSID  
     if (mi.in.get("DSID") != null) {
        inDSID = mi.in.get("DSID") 
     } else {
        inDSID = 0        
     }
     
     // Species
     String inSPEC
     if (mi.in.get("SPEC") != null) {
        inSPEC = mi.inData.get("SPEC").trim() 
     } else {
        inSPEC = ""        
     }

     // Validate Contract Grade Section record
     Optional<DBContainer> EXTCSS = findEXTCSS(inCONO, inDIVI, inDSID, inSPEC)
     if(EXTCSS.isPresent()){
        mi.error("Contract Section Species already exists")   
        return             
     } else {
        findLastNumber()
        inSSID = lastNumber + 1
        // Write record 
        addEXTCSSRecord(inCONO, inDIVI, inDSID, inSPEC)          
     }  

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("SSID", String.valueOf(inSSID))      
     mi.write()

  }
  
  
   //******************************************************************** 
   // Find last id number used
   //********************************************************************  
   void findLastNumber(){   
     
     numberFound = false
     lastNumber = 0

     ExpressionFactory expression = database.getExpressionFactory("EXTCSS")
     
     expression = expression.eq("EXCONO", String.valueOf(inCONO)).and(expression.eq("EXDIVI", inDIVI))
     
     // Get Last Number
     DBAction actionline = database.table("EXTCSS")
     .index("10")
     .matching(expression)
     .selection("EXSSID")
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
        lastNumber = line.get("EXSSID") 
        numberFound = true
      }
  
  }

  //******************************************************************** 
  // Get EXTCSS record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCSS(int CONO, String DIVI, int DSID, String SPEC){  
     DBAction query = database.table("EXTCSS").index("00").build()
     DBContainer EXTCSS = query.getContainer()
     EXTCSS.set("EXCONO", CONO)
     EXTCSS.set("EXDIVI", DIVI)
     EXTCSS.set("EXDSID", DSID)
     EXTCSS.set("EXSPEC", SPEC)
     if(query.read(EXTCSS))  { 
       return Optional.of(EXTCSS)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTCSS record 
  //********************************************************************     
  void addEXTCSSRecord(int CONO, String DIVI, int DSID, String SPEC){     
       DBAction action = database.table("EXTCSS").index("00").build()
       DBContainer EXTCSS = action.createContainer()
       EXTCSS.set("EXCONO", CONO)
       EXTCSS.set("EXDIVI", DIVI)
       EXTCSS.set("EXDSID", DSID)
       EXTCSS.set("EXSPEC", SPEC)   
       EXTCSS.set("EXCHID", program.getUser())
       EXTCSS.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTCSS.set("EXRGDT", regdate) 
       EXTCSS.set("EXLMDT", regdate) 
       EXTCSS.set("EXRGTM", regtime)
       action.insert(EXTCSS)         
 } 

     
} 

