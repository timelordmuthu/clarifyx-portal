"use client";

import React from "react";

/**
 * Finance Dashboard
 * Shows forms awaiting invoice processing (APPROVED).
 */
export default function FinanceDashboard() {
  // Mock data for UI scaffolding
  const approvedForms = [
    { id: 103, customer: "GLOBAL TECH", rep: "Nitin", amount: "₹80,000", status: "APPROVED", approvedOn: "2026-06-08" },
  ];

  return (
    <div className="max-w-6xl mx-auto p-6 mt-10 bg-white shadow rounded-lg">
      <h1 className="text-2xl font-bold text-gray-800 mb-6 border-b pb-4">Finance Processing Dashboard</h1>

      <div className="overflow-x-auto">
        <table className="min-w-full bg-white border border-gray-200">
          <thead className="bg-gray-50 text-left">
            <tr>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Req ID</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Approved On</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Customer</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Amount</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Status</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Action</th>
            </tr>
          </thead>
          <tbody>
            {approvedForms.length === 0 ? (
              <tr>
                <td colSpan={6} className="text-center py-6 text-gray-500">No invoices pending processing.</td>
              </tr>
            ) : (
              approvedForms.map((form) => (
                <tr key={form.id} className="hover:bg-gray-50">
                  <td className="py-3 px-4 border-b text-gray-800">#{form.id}</td>
                  <td className="py-3 px-4 border-b text-gray-600">{form.approvedOn}</td>
                  <td className="py-3 px-4 border-b text-gray-800 font-medium">{form.customer}</td>
                  <td className="py-3 px-4 border-b text-gray-600">{form.amount}</td>
                  <td className="py-3 px-4 border-b">
                    <span className="px-2 py-1 text-xs rounded font-semibold bg-green-100 text-green-800">
                      {form.status}
                    </span>
                  </td>
                  <td className="py-3 px-4 border-b">
                    <button className="bg-green-600 text-white px-3 py-1 rounded shadow hover:bg-green-700 transition text-sm">
                      Process Invoice
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
