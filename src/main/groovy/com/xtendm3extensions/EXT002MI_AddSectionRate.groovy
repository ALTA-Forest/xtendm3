// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add contract section rate to EXTCSR
// Transaction AddSectionRate
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/*
 * Date                      Changed By                          Description
 * 2023-04-10                Jessica Bjorklund                   Version 1 - intial approval
 * 2024-10-31                Jessica Bjorklund                   Version 2 - Change to get the last number from a number series instead
 */
 

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DSID - Section ID
 * @param: CRSQ - Rate Sequence
 * @param: CRML - Min Length (ft)
 * @param: CRXL - Max Length (ft)
 * @param: CRMD - Min Diameter (in)
 * @param: CRXD - Max Diameter (in)
 * @param: CRRA - Amount ($/mbf)
 * @param: CRNO - Note
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: SRID - Rate ID
*/

public class AddSectionRate extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inSRID
  String nextNumber

  
  // Constructor 
  public AddSectionRate(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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
     
     // Rate Sequence
     int inCRSQ 
     if (mi.in.get("CRSQ") != null) {
        inCRSQ = mi.in.get("CRSQ") 
     } else {
        inCRSQ = 0        
     }

     // Min Length
     double inCRML  
     if (mi.in.get("CRML") != null) {
        inCRML = mi.in.get("CRML") 
     } else {
        inCRML = 0d        
     }

     // Max Length
     double inCRXL  
     if (mi.in.get("CRXL") != null) {
        inCRXL = mi.in.get("CRXL") 
     } else {
        inCRXL = 0d        
     }

     // Min Diameter
     double inCRMD  
     if (mi.in.get("CRMD") != null) {
        inCRMD = mi.in.get("CRMD") 
     } else {
        inCRMD = 0d        
     }

     // Max Diameter
     double inCRXD  
     if (mi.in.get("CRXD") != null) {
        inCRXD = mi.in.get("CRXD") 
     } else {
        inCRXD = 0d        
     }

     // Amount
     double inCRRA  
     if (mi.in.get("CRRA") != null) {
        inCRRA = mi.in.get("CRRA") 
     } else {
        inCRRA = 0d        
     }

     // Note
     String inCRNO
     if (mi.inData.get("CRNO") != null) {
        inCRNO = mi.inData.get("CRNO").trim() 
     } else {
        inCRNO = ""        
     }

     // Validate Contract Section Rate record
     Optional<DBContainer> EXTCSR = findEXTCSR(inCONO, inDIVI, inDSID, inCRSQ)
     if(EXTCSR.isPresent()){
        mi.error("Contract Section Rate already exists")   
        return             
     } else {
        //Get next number
        getNextNumber("", "LD", "1") 
        inSRID = nextNumber as Integer
        // Write record 
        addEXTCSRRecord(inCONO, inDIVI, inSRID, inDSID, inCRSQ, inCRML, inCRXL, inCRMD, inCRXD, inCRRA, inCRNO)          
     }  

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("SRID", String.valueOf(inSRID))      
     mi.write()

  }
  

   //***************************************************************************** 
   // Get next number in the number serie using CRS165MI.RtvNextNumber    
   // Input 
   // Division
   // Number Series Type
   // Number Seriew
   //***************************************************************************** 
   void getNextNumber(String division, String numberSeriesType, String numberSeries){   
        Map<String, String> params = [DIVI: division, NBTY: numberSeriesType, NBID: numberSeries] 
        Closure<?> callback = {
        Map<String, String> response ->
          if(response.NBNR != null){
            nextNumber = response.NBNR
          }
        }

        miCaller.call("CRS165MI","RtvNextNumber", params, callback)
   } 


  //******************************************************************** 
  // Get EXTCSR record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCSR(int CONO, String DIVI, int DSID, int CRSQ){  
     DBAction query = database.table("EXTCSR").index("00").build()
     DBContainer EXTCSR = query.getContainer()
     EXTCSR.set("EXCONO", CONO)
     EXTCSR.set("EXDIVI", DIVI)
     EXTCSR.set("EXDSID", DSID)
     EXTCSR.set("EXCRSQ", CRSQ)
     if(query.read(EXTCSR))  { 
       return Optional.of(EXTCSR)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTCSR record 
  //********************************************************************     
  void addEXTCSRRecord(int CONO, String DIVI, int SRID, int DSID, int CRSQ, double CRML, double CRXL, double CRMD, double CRXD, double CRRA, String CRNO){     
       DBAction action = database.table("EXTCSR").index("00").build()
       DBContainer EXTCSR = action.createContainer()
       EXTCSR.set("EXCONO", CONO)
       EXTCSR.set("EXDIVI", DIVI)
       EXTCSR.set("EXSRID", SRID)
       EXTCSR.set("EXDSID", DSID)
       EXTCSR.set("EXCRSQ", CRSQ)
       EXTCSR.set("EXCRML", CRML)
       EXTCSR.set("EXCRXL", CRXL)
       EXTCSR.set("EXCRMD", CRMD)
       EXTCSR.set("EXCRXD", CRXD)
       EXTCSR.set("EXCRRA", CRRA)
       EXTCSR.set("EXCRNO", CRNO)  
       EXTCSR.set("EXCHID", program.getUser())
       EXTCSR.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTCSR.set("EXRGDT", regdate) 
       EXTCSR.set("EXLMDT", regdate) 
       EXTCSR.set("EXRGTM", regtime)
       action.insert(EXTCSR)         
 } 

     
} 

