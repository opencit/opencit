/**
 * Copyright (C) 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
 * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3) Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef NIARL_UTIL_BYTEBLOB_H
#define NIARL_UTIL_BYTEBLOB_H

// ************ MODULE HEADERS ************ //
#include "NIARL_TPM_ModuleV2.h"

// ************ TSS HEADERS ************ //
#include <tss/tspi.h>
#include <tss/tss_error.h>
#include <tss/tss_defines.h>
//#include "tspi.h"
//#include "tss_error.h"
//#include "tss_defines.h"

// ************ STANDARD HEADERS ************ //
#include <string>
#include <string.h>
#include <iostream>
#include <iomanip>
using namespace std;

class NIARL_Util_ByteBlob
{
public:
	UINT32		size;
	BYTE*		blob;

	NIARL_Util_ByteBlob(string in_var);
	~NIARL_Util_ByteBlob();

	void		Print();
};

#endif
